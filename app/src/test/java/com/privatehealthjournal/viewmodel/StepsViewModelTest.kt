package com.privatehealthjournal.viewmodel

import android.app.Application
import com.privatehealthjournal.data.entity.StepCountEntry
import com.privatehealthjournal.data.entity.StepSource
import com.privatehealthjournal.data.preferences.AppPreferences
import com.privatehealthjournal.data.preferences.BudgetPreferences
import com.privatehealthjournal.data.repository.StepsRepository
import com.privatehealthjournal.util.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
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
class StepsViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val repository: StepsRepository = mockk(relaxed = true) {
        every { allStepCountEntries } returns MutableStateFlow(emptyList())
        every { getRecentStepCountEntries(any()) } returns flowOf(emptyList())
    }
    private val application: Application = mockk(relaxed = true)

    @Before
    fun stubPreferences() {
        mockkObject(AppPreferences)
        mockkObject(BudgetPreferences)
        every { AppPreferences.getShowStepCounting(any()) } returns flowOf(false)
        every { AppPreferences.getStepSensorEnabled(any()) } returns flowOf(false)
        every { AppPreferences.getHealthConnectEnabled(any()) } returns flowOf(false)
        every { BudgetPreferences.getStepsPerPointCredit(any()) } returns flowOf(null)
    }

    @After
    fun unstub() {
        unmockkObject(AppPreferences)
        unmockkObject(BudgetPreferences)
    }

    @Test
    fun `addStepCount writes MANUAL source`() = runTest {
        val vm = StepsViewModel(repository, application)

        vm.addStepCount(dateEpochDay = 20_000L, steps = 8500, notes = "n")
        advanceUntilIdle()

        coVerify {
            repository.recordStepCount(
                dateEpochDay = 20_000L,
                steps = 8500,
                source = StepSource.MANUAL,
                notes = "n",
                timestamp = any(),
            )
        }
    }

    @Test
    fun `deleteStepCount forwards to repository`() = runTest {
        val vm = StepsViewModel(repository, application)
        val entry = StepCountEntry(
            id = 5L, dateEpochDay = 20_000L, steps = 1000, source = StepSource.MANUAL
        )

        vm.deleteStepCount(entry)
        advanceUntilIdle()

        coVerify { repository.deleteStepCount(entry) }
    }

    @Test
    fun `loadStepCountForEditing populates editing state`() = runTest {
        val entry = StepCountEntry(
            id = 7L, dateEpochDay = 20_000L, steps = 5555, source = StepSource.HEALTH_CONNECT
        )
        io.mockk.coEvery { repository.getStepCountById(7L) } returns entry
        val vm = StepsViewModel(repository, application)

        vm.loadStepCountForEditing(7L)
        advanceUntilIdle()

        assert(vm.editingStepCount.value === entry)
    }
}
