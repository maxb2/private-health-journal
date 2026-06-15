package com.privatehealthjournal.viewmodel

import android.app.Application
import com.privatehealthjournal.data.export.ExportData
import com.privatehealthjournal.data.export.ExportedMedicationSet
import com.privatehealthjournal.data.export.ExportedMedicationSetItem
import com.privatehealthjournal.data.export.ExportedMedicationSetReminder
import com.privatehealthjournal.data.export.ImportResult
import com.privatehealthjournal.data.repository.BiometricsRepository
import com.privatehealthjournal.data.repository.JournalRepository
import com.privatehealthjournal.data.repository.MedicationRepository
import com.privatehealthjournal.data.repository.StepsRepository
import com.privatehealthjournal.di.TransactionRunner
import com.privatehealthjournal.notification.ReminderScheduler
import com.privatehealthjournal.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val journal: JournalRepository = mockk(relaxed = true) {
        every { allMeals } returns flowOf(emptyList())
        every { allSymptomEntries } returns flowOf(emptyList())
        every { allBowelMovements } returns flowOf(emptyList())
        every { allOtherEntries } returns flowOf(emptyList())
        every { allCycleEntries } returns flowOf(emptyList())
    }
    private val medication: MedicationRepository = mockk(relaxed = true) {
        every { allMedications } returns flowOf(emptyList())
        every { allMedicationSets } returns flowOf(emptyList())
        every { getAllReminders() } returns flowOf(emptyList())
        every { getAllMedicationSetLogs() } returns flowOf(emptyList())
    }
    private val biometrics: BiometricsRepository = mockk(relaxed = true) {
        every { allBloodPressureEntries } returns flowOf(emptyList())
        every { allCholesterolEntries } returns flowOf(emptyList())
        every { allWeightEntries } returns flowOf(emptyList())
        every { allSpO2Entries } returns flowOf(emptyList())
        every { allBloodGlucoseEntries } returns flowOf(emptyList())
    }
    private val steps: StepsRepository = mockk(relaxed = true) {
        every { allStepCountEntries } returns flowOf(emptyList())
    }

    /** Verifies tx { ... } actually wraps the block and the block runs exactly once. */
    private class CountingTransactionRunner : TransactionRunner(mockk(relaxed = true)) {
        var invocations = 0
        override suspend fun <T> invoke(block: suspend () -> T): T {
            invocations++
            return block()
        }
    }

    /** Tx that throws instead of running the block — proves the tx boundary actually owns rollback. */
    private class FailingTransactionRunner : TransactionRunner(mockk(relaxed = true)) {
        override suspend fun <T> invoke(block: suspend () -> T): T {
            throw RuntimeException("simulated rollback")
        }
    }

    private val application: Application = mockk(relaxed = true)

    @Before
    fun stubScheduler() {
        mockkObject(ReminderScheduler)
        coEvery { ReminderScheduler.rescheduleAllReminders(any()) } just Runs
    }

    @After
    fun unstub() {
        unmockkObject(ReminderScheduler)
    }

    @Test
    fun `applyImport runs DataImporter inside transaction exactly once`() = runTest {
        val tx = CountingTransactionRunner()
        val vm = SettingsViewModel(journal, medication, biometrics, steps, tx, application)

        val result = vm.applyImport(Gson().toJson(ExportData()))

        assertThat(result).isInstanceOf(ImportResult.Success::class.java)
        assertThat(tx.invocations).isEqualTo(1)
        coVerify(exactly = 0) { ReminderScheduler.rescheduleAllReminders(any()) }
    }

    @Test
    fun `applyImport with reminders triggers rescheduleAllReminders`() = runTest {
        coEvery { medication.insertMedicationSet(any(), any()) } returns 1L
        coEvery { medication.insertReminder(any()) } returns 1L
        val vm = SettingsViewModel(journal, medication, biometrics, steps, CountingTransactionRunner(), application)
        val export = ExportData(
            medicationSets = listOf(
                ExportedMedicationSet(
                    name = "Morning",
                    items = listOf(ExportedMedicationSetItem("Vitamin D", "1000IU")),
                    reminders = listOf(
                        ExportedMedicationSetReminder(hour = 9, minute = 0, daysOfWeek = 127, enabled = true)
                    ),
                )
            )
        )

        val result = vm.applyImport(Gson().toJson(export))

        assertThat(result).isInstanceOf(ImportResult.Success::class.java)
        coVerify(exactly = 1) { ReminderScheduler.rescheduleAllReminders(application) }
    }

    @Test
    fun `applyImport returns Error for invalid JSON without rescheduling`() = runTest {
        val vm = SettingsViewModel(journal, medication, biometrics, steps, CountingTransactionRunner(), application)

        val result = vm.applyImport("not valid json")

        assertThat(result).isInstanceOf(ImportResult.Error::class.java)
        coVerify(exactly = 0) { ReminderScheduler.rescheduleAllReminders(any()) }
    }

    @Test
    fun `applyImport surfaces transaction failure as Error`() = runTest {
        val vm = SettingsViewModel(journal, medication, biometrics, steps, FailingTransactionRunner(), application)

        try {
            vm.applyImport(Gson().toJson(ExportData()))
            error("Expected exception to propagate")
        } catch (e: RuntimeException) {
            assertThat(e.message).isEqualTo("simulated rollback")
        }
        coVerify(exactly = 0) { ReminderScheduler.rescheduleAllReminders(any()) }
    }
}
