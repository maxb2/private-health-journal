package com.privatehealthjournal.viewmodel

import android.app.Application
import com.privatehealthjournal.data.entity.MealType
import com.privatehealthjournal.data.entity.OtherEntryType
import com.privatehealthjournal.data.preferences.AppPreferences
import com.privatehealthjournal.data.preferences.BudgetPreferences
import com.privatehealthjournal.data.repository.JournalRepository
import com.privatehealthjournal.util.MainDispatcherRule
import com.privatehealthjournal.util.TestData
import io.mockk.coEvery
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
class JournalViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val repository: JournalRepository = mockk(relaxed = true) {
        every { allMeals } returns MutableStateFlow(emptyList())
        every { allTags } returns MutableStateFlow(emptyList())
        every { allFoodNames } returns MutableStateFlow(emptyList())
        every { allSymptomEntries } returns MutableStateFlow(emptyList())
        every { ongoingSymptoms } returns MutableStateFlow(emptyList())
        every { allSymptomNames } returns MutableStateFlow(emptyList())
        every { allBowelMovements } returns MutableStateFlow(emptyList())
        every { allOtherEntries } returns MutableStateFlow(emptyList())
        every { allCycleEntries } returns MutableStateFlow(emptyList())
        every { getRecentMeals(any()) } returns flowOf(emptyList())
        every { getRecentSymptomEntries(any()) } returns flowOf(emptyList())
        every { getRecentBowelMovements(any()) } returns flowOf(emptyList())
        every { getRecentOtherEntries(any()) } returns flowOf(emptyList())
        every { getRecentCycleEntries(any()) } returns flowOf(emptyList())
        every { getDistinctOtherSubTypes(any()) } returns flowOf(emptyList())
    }
    private val application: Application = mockk(relaxed = true)

    @Before
    fun stubPreferences() {
        mockkObject(AppPreferences)
        mockkObject(BudgetPreferences)
        every { AppPreferences.getShowCycleTracking(any()) } returns flowOf(true)
        every { BudgetPreferences.getDailyBudget(any()) } returns flowOf(null)
    }

    @After
    fun unstub() {
        unmockkObject(AppPreferences)
        unmockkObject(BudgetPreferences)
    }

    @Test
    fun `addMeal forwards args to repository`() = runTest {
        val vm = JournalViewModel(repository, application)

        vm.addMeal(
            mealType = MealType.LUNCH,
            foods = listOf("Salad", "Soup"),
            tags = listOf("Healthy"),
            notes = "good",
            timestamp = 1234L,
            pointCost = 5,
        )
        advanceUntilIdle()

        coVerify {
            repository.insertMeal(
                mealType = MealType.LUNCH,
                foods = listOf("Salad", "Soup"),
                tags = listOf("Healthy"),
                notes = "good",
                timestamp = 1234L,
                pointCost = 5,
            )
        }
    }

    @Test
    fun `addSymptom builds entry with provided fields`() = runTest {
        val vm = JournalViewModel(repository, application)

        vm.addSymptom(name = "Migraine", severity = 5, startTime = 999L, endTime = null, notes = "bad")
        advanceUntilIdle()

        coVerify {
            repository.insertSymptom(match {
                it.name == "Migraine" && it.severity == 5 && it.startTime == 999L &&
                    it.endTime == null && it.notes == "bad"
            })
        }
    }

    @Test
    fun `endSymptom delegates to repository with entry id`() = runTest {
        val vm = JournalViewModel(repository, application)
        val symptom = TestData.createSymptomEntry(id = 11L)

        vm.endSymptom(symptom)
        advanceUntilIdle()

        coVerify { repository.endSymptom(id = 11L, endTime = any()) }
    }

    @Test
    fun `loadMealForEditing populates editing state`() = runTest {
        val meal = TestData.createMealWithDetails(meal = TestData.createMealEntry(id = 5L))
        coEvery { repository.getMealWithDetailsById(5L) } returns meal
        val vm = JournalViewModel(repository, application)

        vm.loadMealForEditing(5L)
        advanceUntilIdle()

        assert(vm.editingMeal.value === meal)
    }

    @Test
    fun `clearing one editing flow does not wipe the others`() = runTest {
        val meal = TestData.createMealWithDetails()
        val symptom = TestData.createSymptomEntry()
        val other = TestData.createOtherEntry(entryType = OtherEntryType.SLEEP)
        coEvery { repository.getMealWithDetailsById(any()) } returns meal
        coEvery { repository.getSymptomById(any()) } returns symptom
        coEvery { repository.getOtherEntryById(any()) } returns other
        val vm = JournalViewModel(repository, application)

        vm.loadMealForEditing(1L)
        vm.loadSymptomForEditing(1L)
        vm.loadOtherEntryForEditing(1L)
        advanceUntilIdle()
        vm.clearEditingMeal()

        assert(vm.editingMeal.value == null)
        assert(vm.editingSymptom.value === symptom)
        assert(vm.editingOtherEntry.value === other)
    }
}
