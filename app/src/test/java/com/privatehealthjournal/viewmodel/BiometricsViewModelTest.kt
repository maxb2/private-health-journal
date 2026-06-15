package com.privatehealthjournal.viewmodel

import android.app.Application
import com.privatehealthjournal.data.entity.GlucoseMealContext
import com.privatehealthjournal.data.entity.GlucoseUnit
import com.privatehealthjournal.data.entity.WeightUnit
import com.privatehealthjournal.data.repository.BiometricsRepository
import com.privatehealthjournal.util.MainDispatcherRule
import com.privatehealthjournal.util.TestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BiometricsViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val repository: BiometricsRepository = mockk(relaxed = true) {
        every { allBloodPressureEntries } returns MutableStateFlow(emptyList())
        every { allCholesterolEntries } returns MutableStateFlow(emptyList())
        every { allWeightEntries } returns MutableStateFlow(emptyList())
        every { allSpO2Entries } returns MutableStateFlow(emptyList())
        every { allBloodGlucoseEntries } returns MutableStateFlow(emptyList())
        every { getRecentBloodPressureEntries(any()) } returns flowOf(emptyList())
        every { getRecentCholesterolEntries(any()) } returns flowOf(emptyList())
        every { getRecentWeightEntries(any()) } returns flowOf(emptyList())
        every { getRecentSpO2Entries(any()) } returns flowOf(emptyList())
        every { getRecentBloodGlucoseEntries(any()) } returns flowOf(emptyList())
    }
    private val application: Application = mockk(relaxed = true)

    @Test
    fun `addBloodPressure builds and inserts entry`() = runTest {
        val vm = BiometricsViewModel(repository, application)

        vm.addBloodPressure(systolic = 130, diastolic = 85, pulse = 70, notes = "n", timestamp = 1L)
        advanceUntilIdle()

        coVerify {
            repository.insertBloodPressure(match {
                it.systolic == 130 && it.diastolic == 85 && it.pulse == 70 &&
                    it.notes == "n" && it.timestamp == 1L
            })
        }
    }

    @Test
    fun `addWeight defaults to LB when unit omitted`() = runTest {
        val vm = BiometricsViewModel(repository, application)

        vm.addWeight(weight = 150.0)
        advanceUntilIdle()

        coVerify {
            repository.insertWeight(match {
                it.weight == 150.0 && it.unit == WeightUnit.LB
            })
        }
    }

    @Test
    fun `addBloodGlucose preserves unit and meal context`() = runTest {
        val vm = BiometricsViewModel(repository, application)

        vm.addBloodGlucose(
            glucoseLevel = 95.0,
            unit = GlucoseUnit.MMOL_L,
            mealContext = GlucoseMealContext.AFTER_MEAL,
            timestamp = 5L,
        )
        advanceUntilIdle()

        coVerify {
            repository.insertBloodGlucose(match {
                it.glucoseLevel == 95.0 &&
                    it.unit == GlucoseUnit.MMOL_L &&
                    it.mealContext == GlucoseMealContext.AFTER_MEAL
            })
        }
    }

    @Test
    fun `loadBloodPressureForEditing populates editing state`() = runTest {
        val entry = TestData.createBloodPressureEntry(id = 9L)
        coEvery { repository.getBloodPressureById(9L) } returns entry
        val vm = BiometricsViewModel(repository, application)

        vm.loadBloodPressureForEditing(9L)
        advanceUntilIdle()

        assert(vm.editingBloodPressure.value === entry)
    }

    @Test
    fun `clearEditingBloodPressure does not wipe other editing flows`() = runTest {
        val bp = TestData.createBloodPressureEntry()
        val chol = TestData.createCholesterolEntry()
        val w = TestData.createWeightEntry()
        coEvery { repository.getBloodPressureById(any()) } returns bp
        coEvery { repository.getCholesterolById(any()) } returns chol
        coEvery { repository.getWeightById(any()) } returns w
        val vm = BiometricsViewModel(repository, application)

        vm.loadBloodPressureForEditing(1L)
        vm.loadCholesterolForEditing(1L)
        vm.loadWeightForEditing(1L)
        advanceUntilIdle()
        vm.clearEditingBloodPressure()

        assert(vm.editingBloodPressure.value == null)
        assert(vm.editingCholesterol.value === chol)
        assert(vm.editingWeight.value === w)
    }
}
