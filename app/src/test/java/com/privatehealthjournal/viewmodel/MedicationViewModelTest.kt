package com.privatehealthjournal.viewmodel

import android.app.Application
import com.privatehealthjournal.data.entity.MedicationSet
import com.privatehealthjournal.data.entity.MedicationSetItem
import com.privatehealthjournal.data.entity.MedicationSetReminder
import com.privatehealthjournal.data.entity.MedicationSetWithItems
import com.privatehealthjournal.data.repository.MedicationRepository
import com.privatehealthjournal.notification.ReminderScheduler
import com.privatehealthjournal.util.MainDispatcherRule
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MedicationViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val repository: MedicationRepository = mockk(relaxed = true) {
        every { allMedications } returns MutableStateFlow(emptyList())
        every { allMedicationNames } returns MutableStateFlow(emptyList())
        every { allMedicationSets } returns MutableStateFlow(emptyList())
        every { getAllReminders() } returns flowOf(emptyList())
        every { getRecentMedications(any()) } returns flowOf(emptyList())
    }
    private val application: Application = mockk(relaxed = true)

    @Before
    fun stubScheduler() {
        mockkObject(ReminderScheduler)
        every { ReminderScheduler.scheduleReminder(any(), any()) } just Runs
        every { ReminderScheduler.cancelReminder(any(), any()) } just Runs
        every { ReminderScheduler.dismissNotification(any(), any()) } just Runs
    }

    @After
    fun unstub() {
        unmockkObject(ReminderScheduler)
    }

    @Test
    fun `logMedicationSet maps items and calls atomic logger then dismisses notification`() = runTest {
        coEvery { repository.logMedicationSetAtomically(any(), any(), any(), any()) } just Runs
        val vm = MedicationViewModel(repository, application)
        val setWithItems = MedicationSetWithItems(
            set = MedicationSet(id = 7L, name = "Evening"),
            items = listOf(
                MedicationSetItem(id = 1, setId = 7L, name = "Atorvastatin", dosage = "20mg"),
                MedicationSetItem(id = 2, setId = 7L, name = "Metformin", dosage = "500mg"),
            )
        )

        vm.logMedicationSet(setWithItems)
        advanceUntilIdle()

        coVerify {
            repository.logMedicationSetAtomically(
                setId = 7L,
                items = listOf(
                    MedicationRepository.MedicationSetItemSpec("Atorvastatin", "20mg"),
                    MedicationRepository.MedicationSetItemSpec("Metformin", "500mg"),
                ),
                timestamp = any(),
                notes = "Logged from set: Evening",
            )
        }
        verify { ReminderScheduler.dismissNotification(application, 7L) }
    }

    @Test
    fun `addReminder schedules with the saved id`() = runTest {
        coEvery { repository.insertReminder(any()) } returns 99L
        val vm = MedicationViewModel(repository, application)
        val reminder = MedicationSetReminder(
            id = 0L, setId = 7L, hour = 20, minute = 0, daysOfWeek = 127, enabled = true
        )

        vm.addReminder(reminder)
        advanceUntilIdle()

        verify { ReminderScheduler.scheduleReminder(application, match { it.id == 99L && it.setId == 7L }) }
    }

    @Test
    fun `updateReminder schedules when enabled`() = runTest {
        val vm = MedicationViewModel(repository, application)
        val reminder = MedicationSetReminder(
            id = 5L, setId = 7L, hour = 8, minute = 0, daysOfWeek = 127, enabled = true
        )

        vm.updateReminder(reminder)
        advanceUntilIdle()

        coVerify { repository.updateReminder(reminder) }
        verify { ReminderScheduler.scheduleReminder(application, reminder) }
        verify(exactly = 0) { ReminderScheduler.cancelReminder(any(), any()) }
    }

    @Test
    fun `updateReminder cancels when disabled`() = runTest {
        val vm = MedicationViewModel(repository, application)
        val reminder = MedicationSetReminder(
            id = 5L, setId = 7L, hour = 8, minute = 0, daysOfWeek = 127, enabled = false
        )

        vm.updateReminder(reminder)
        advanceUntilIdle()

        coVerify { repository.updateReminder(reminder) }
        verify { ReminderScheduler.cancelReminder(application, 5L) }
        verify(exactly = 0) { ReminderScheduler.scheduleReminder(any(), any()) }
    }

    @Test
    fun `deleteReminder cancels before deleting`() = runTest {
        val vm = MedicationViewModel(repository, application)
        val reminder = MedicationSetReminder(
            id = 5L, setId = 7L, hour = 8, minute = 0, daysOfWeek = 127, enabled = true
        )

        vm.deleteReminder(reminder)
        advanceUntilIdle()

        verify { ReminderScheduler.cancelReminder(application, 5L) }
        coVerify { repository.deleteReminder(reminder) }
    }
}
