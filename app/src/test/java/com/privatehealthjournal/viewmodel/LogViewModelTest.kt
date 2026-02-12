package com.privatehealthjournal.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.privatehealthjournal.data.AppDatabase
import com.privatehealthjournal.data.dao.BowelMovementDao
import com.privatehealthjournal.data.dao.MealDao
import com.privatehealthjournal.data.dao.MedicationDao
import com.privatehealthjournal.data.dao.OtherEntryDao
import com.privatehealthjournal.data.dao.SymptomEntryDao
import com.privatehealthjournal.data.entity.MealType
import com.privatehealthjournal.data.repository.LogRepository
import com.privatehealthjournal.util.TestData
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LogViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var application: Application
    private lateinit var database: AppDatabase
    private lateinit var mealDao: MealDao
    private lateinit var symptomEntryDao: SymptomEntryDao
    private lateinit var bowelMovementDao: BowelMovementDao
    private lateinit var medicationDao: MedicationDao
    private lateinit var otherEntryDao: OtherEntryDao
    private lateinit var viewModel: LogViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        application = mockk(relaxed = true)
        database = mockk(relaxed = true)
        mealDao = mockk(relaxed = true)
        symptomEntryDao = mockk(relaxed = true)
        bowelMovementDao = mockk(relaxed = true)
        medicationDao = mockk(relaxed = true)
        otherEntryDao = mockk(relaxed = true)

        every { database.mealDao() } returns mealDao
        every { database.symptomEntryDao() } returns symptomEntryDao
        every { database.bowelMovementDao() } returns bowelMovementDao
        every { database.medicationDao() } returns medicationDao
        every { database.otherEntryDao() } returns otherEntryDao

        every { mealDao.getAllMealsWithDetails() } returns flowOf(emptyList())
        every { mealDao.getRecentMealsWithDetails(any()) } returns flowOf(emptyList())
        every { mealDao.getAllTags() } returns flowOf(emptyList())
        every { symptomEntryDao.getAllSymptomEntries() } returns flowOf(emptyList())
        every { symptomEntryDao.getRecentSymptomEntries(any()) } returns flowOf(emptyList())
        every { symptomEntryDao.getOngoingSymptoms() } returns flowOf(emptyList())
        every { bowelMovementDao.getAllBowelMovements() } returns flowOf(emptyList())
        every { bowelMovementDao.getRecentBowelMovements(any()) } returns flowOf(emptyList())
        every { medicationDao.getAllMedications() } returns flowOf(emptyList())
        every { medicationDao.getRecentMedications(any()) } returns flowOf(emptyList())
        every { medicationDao.getAllMedicationNames() } returns flowOf(emptyList())
        every { otherEntryDao.getAllOtherEntries() } returns flowOf(emptyList())
        every { otherEntryDao.getRecentOtherEntries(any()) } returns flowOf(emptyList())

        mockkObject(AppDatabase)
        every { AppDatabase.getDatabase(any()) } returns database

        viewModel = LogViewModel(application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkObject(AppDatabase)
    }

    @Test
    fun `initial state flows are empty lists`() = runTest {
        advanceUntilIdle()

        assertThat(viewModel.allMeals.value).isEmpty()
        assertThat(viewModel.allSymptomEntries.value).isEmpty()
        assertThat(viewModel.allBowelMovements.value).isEmpty()
        assertThat(viewModel.allMedications.value).isEmpty()
        assertThat(viewModel.allOtherEntries.value).isEmpty()
        assertThat(viewModel.ongoingSymptoms.value).isEmpty()
    }

    @Test
    fun `addMeal calls repository insertMeal`() = runTest {
        coEvery { mealDao.insertMealWithDetails(any(), any(), any()) } returns 1L

        viewModel.addMeal(
            mealType = MealType.BREAKFAST,
            foods = listOf("Eggs"),
            tags = listOf("Healthy"),
            notes = "Test"
        )
        advanceUntilIdle()

        coVerify { mealDao.insertMealWithDetails(any(), listOf("Eggs"), listOf("Healthy")) }
    }

    @Test
    fun `addSymptom calls repository insertSymptom`() = runTest {
        coEvery { symptomEntryDao.insert(any()) } returns 1L

        viewModel.addSymptom(
            name = "Headache",
            severity = 3,
            notes = "Test"
        )
        advanceUntilIdle()

        coVerify { symptomEntryDao.insert(match { it.name == "Headache" && it.severity == 3 }) }
    }

    @Test
    fun `addBowelMovement calls repository insertBowelMovement`() = runTest {
        coEvery { bowelMovementDao.insert(any()) } returns 1L

        viewModel.addBowelMovement(bristolType = 4, notes = "Test")
        advanceUntilIdle()

        coVerify { bowelMovementDao.insert(match { it.bristolType == 4 }) }
    }

    @Test
    fun `addMedication calls repository insertMedication`() = runTest {
        coEvery { medicationDao.insert(any()) } returns 1L

        viewModel.addMedication(name = "Ibuprofen", dosage = "200mg")
        advanceUntilIdle()

        coVerify { medicationDao.insert(match { it.name == "Ibuprofen" && it.dosage == "200mg" }) }
    }

    @Test
    fun `addOtherEntry calls repository insertOtherEntry`() = runTest {
        val entry = TestData.createOtherEntry()
        coEvery { otherEntryDao.insert(any()) } returns 1L

        viewModel.addOtherEntry(entry)
        advanceUntilIdle()

        coVerify { otherEntryDao.insert(entry) }
    }

    @Test
    fun `endSymptom calls repository endSymptom`() = runTest {
        val symptom = TestData.createSymptomEntry(id = 5L)

        viewModel.endSymptom(symptom)
        advanceUntilIdle()

        coVerify { symptomEntryDao.updateEndTime(5L, any()) }
    }

    @Test
    fun `deleteMeal calls repository deleteMeal`() = runTest {
        val mealWithDetails = TestData.createMealWithDetails()

        viewModel.deleteMeal(mealWithDetails)
        advanceUntilIdle()

        coVerify { mealDao.deleteMeal(mealWithDetails.meal) }
    }

    @Test
    fun `deleteSymptom calls repository deleteSymptom`() = runTest {
        val symptom = TestData.createSymptomEntry()

        viewModel.deleteSymptom(symptom)
        advanceUntilIdle()

        coVerify { symptomEntryDao.delete(symptom) }
    }

    @Test
    fun `deleteBowelMovement calls repository deleteBowelMovement`() = runTest {
        val entry = TestData.createBowelMovementEntry()

        viewModel.deleteBowelMovement(entry)
        advanceUntilIdle()

        coVerify { bowelMovementDao.delete(entry) }
    }

    @Test
    fun `deleteMedication calls repository deleteMedication`() = runTest {
        val entry = TestData.createMedicationEntry()

        viewModel.deleteMedication(entry)
        advanceUntilIdle()

        coVerify { medicationDao.delete(entry) }
    }

    @Test
    fun `deleteOtherEntry calls repository deleteOtherEntry`() = runTest {
        val entry = TestData.createOtherEntry()

        viewModel.deleteOtherEntry(entry)
        advanceUntilIdle()

        coVerify { otherEntryDao.delete(entry) }
    }

    @Test
    fun `updateSymptom calls repository updateSymptom`() = runTest {
        val symptom = TestData.createSymptomEntry()

        viewModel.updateSymptom(symptom)
        advanceUntilIdle()

        coVerify { symptomEntryDao.update(symptom) }
    }

    @Test
    fun `updateBowelMovement calls repository updateBowelMovement`() = runTest {
        val entry = TestData.createBowelMovementEntry()

        viewModel.updateBowelMovement(entry)
        advanceUntilIdle()

        coVerify { bowelMovementDao.update(entry) }
    }

    @Test
    fun `updateMedication calls repository updateMedication`() = runTest {
        val entry = TestData.createMedicationEntry()

        viewModel.updateMedication(entry)
        advanceUntilIdle()

        coVerify { medicationDao.update(entry) }
    }

    @Test
    fun `updateOtherEntry calls repository updateOtherEntry`() = runTest {
        val entry = TestData.createOtherEntry()

        viewModel.updateOtherEntry(entry)
        advanceUntilIdle()

        coVerify { otherEntryDao.update(entry) }
    }

    @Test
    fun `updateMeal calls repository updateMeal`() = runTest {
        val meal = TestData.createMealEntry()
        val foods = listOf("Updated food")
        val tags = listOf("Updated tag")

        viewModel.updateMeal(meal, foods, tags)
        advanceUntilIdle()

        coVerify { mealDao.updateMealWithDetails(meal, foods, tags) }
    }

    @Test
    fun `loadSymptomForEditing updates editingSymptom state`() = runTest {
        val symptom = TestData.createSymptomEntry(id = 1L)
        coEvery { symptomEntryDao.getById(1L) } returns symptom

        viewModel.loadSymptomForEditing(1L)
        advanceUntilIdle()

        assertThat(viewModel.editingSymptom.value).isEqualTo(symptom)
    }

    @Test
    fun `loadBowelMovementForEditing updates editingBowelMovement state`() = runTest {
        val entry = TestData.createBowelMovementEntry(id = 1L)
        coEvery { bowelMovementDao.getById(1L) } returns entry

        viewModel.loadBowelMovementForEditing(1L)
        advanceUntilIdle()

        assertThat(viewModel.editingBowelMovement.value).isEqualTo(entry)
    }

    @Test
    fun `loadMealForEditing updates editingMeal state`() = runTest {
        val mealWithDetails = TestData.createMealWithDetails()
        coEvery { mealDao.getMealWithDetailsById(1L) } returns mealWithDetails

        viewModel.loadMealForEditing(1L)
        advanceUntilIdle()

        assertThat(viewModel.editingMeal.value).isEqualTo(mealWithDetails)
    }

    @Test
    fun `loadMedicationForEditing updates editingMedication state`() = runTest {
        val entry = TestData.createMedicationEntry(id = 1L)
        coEvery { medicationDao.getById(1L) } returns entry

        viewModel.loadMedicationForEditing(1L)
        advanceUntilIdle()

        assertThat(viewModel.editingMedication.value).isEqualTo(entry)
    }

    @Test
    fun `loadOtherEntryForEditing updates editingOtherEntry state`() = runTest {
        val entry = TestData.createOtherEntry(id = 1L)
        coEvery { otherEntryDao.getById(1L) } returns entry

        viewModel.loadOtherEntryForEditing(1L)
        advanceUntilIdle()

        assertThat(viewModel.editingOtherEntry.value).isEqualTo(entry)
    }

    @Test
    fun `clearEditingState clears all editing states`() = runTest {
        // First load some data
        val symptom = TestData.createSymptomEntry(id = 1L)
        coEvery { symptomEntryDao.getById(1L) } returns symptom
        viewModel.loadSymptomForEditing(1L)
        advanceUntilIdle()

        // Verify it's set
        assertThat(viewModel.editingSymptom.value).isNotNull()

        // Clear
        viewModel.clearEditingState()

        // Verify all are null
        assertThat(viewModel.editingSymptom.value).isNull()
        assertThat(viewModel.editingBowelMovement.value).isNull()
        assertThat(viewModel.editingMeal.value).isNull()
        assertThat(viewModel.editingMedication.value).isNull()
        assertThat(viewModel.editingOtherEntry.value).isNull()
    }
}
