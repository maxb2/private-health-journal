package com.foodsymptomlog.repository

import com.foodsymptomlog.data.dao.BloodPressureDao
import com.foodsymptomlog.data.dao.BowelMovementDao
import com.foodsymptomlog.data.dao.CholesterolDao
import com.foodsymptomlog.data.dao.MealDao
import com.foodsymptomlog.data.dao.MedicationDao
import com.foodsymptomlog.data.dao.OtherEntryDao
import com.foodsymptomlog.data.dao.SymptomEntryDao
import com.foodsymptomlog.data.dao.WeightDao
import com.foodsymptomlog.data.entity.MealType
import com.foodsymptomlog.data.repository.LogRepository
import com.foodsymptomlog.util.TestData
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class LogRepositoryTest {

    private lateinit var mealDao: MealDao
    private lateinit var symptomEntryDao: SymptomEntryDao
    private lateinit var bowelMovementDao: BowelMovementDao
    private lateinit var medicationDao: MedicationDao
    private lateinit var otherEntryDao: OtherEntryDao
    private lateinit var bloodPressureDao: BloodPressureDao
    private lateinit var cholesterolDao: CholesterolDao
    private lateinit var weightDao: WeightDao
    private lateinit var repository: LogRepository

    @Before
    fun setup() {
        mealDao = mockk(relaxed = true)
        symptomEntryDao = mockk(relaxed = true)
        bowelMovementDao = mockk(relaxed = true)
        medicationDao = mockk(relaxed = true)
        otherEntryDao = mockk(relaxed = true)
        bloodPressureDao = mockk(relaxed = true)
        cholesterolDao = mockk(relaxed = true)
        weightDao = mockk(relaxed = true)

        every { mealDao.getAllMealsWithDetails() } returns flowOf(emptyList())
        every { mealDao.getAllTags() } returns flowOf(emptyList())
        every { symptomEntryDao.getAllSymptomEntries() } returns flowOf(emptyList())
        every { symptomEntryDao.getOngoingSymptoms() } returns flowOf(emptyList())
        every { bowelMovementDao.getAllBowelMovements() } returns flowOf(emptyList())
        every { medicationDao.getAllMedications() } returns flowOf(emptyList())
        every { medicationDao.getAllMedicationNames() } returns flowOf(emptyList())
        every { otherEntryDao.getAllOtherEntries() } returns flowOf(emptyList())
        every { bloodPressureDao.getAllBloodPressureEntries() } returns flowOf(emptyList())
        every { cholesterolDao.getAllCholesterolEntries() } returns flowOf(emptyList())
        every { weightDao.getAllWeightEntries() } returns flowOf(emptyList())

        repository = LogRepository(
            mealDao,
            symptomEntryDao,
            bowelMovementDao,
            medicationDao,
            otherEntryDao,
            bloodPressureDao,
            cholesterolDao,
            weightDao
        )
    }

    // Insert tests
    @Test
    fun `insertMeal delegates to mealDao`() = runTest {
        coEvery { mealDao.insertMealWithDetails(any(), any(), any()) } returns 1L

        val result = repository.insertMeal(
            mealType = MealType.BREAKFAST,
            foods = listOf("Eggs", "Toast"),
            tags = listOf("Healthy"),
            notes = "Test meal"
        )

        assertThat(result).isEqualTo(1L)
        coVerify { mealDao.insertMealWithDetails(any(), listOf("Eggs", "Toast"), listOf("Healthy")) }
    }

    @Test
    fun `insertSymptom delegates to symptomEntryDao`() = runTest {
        val symptom = TestData.createSymptomEntry()
        coEvery { symptomEntryDao.insert(symptom) } returns 1L

        val result = repository.insertSymptom(symptom)

        assertThat(result).isEqualTo(1L)
        coVerify { symptomEntryDao.insert(symptom) }
    }

    @Test
    fun `insertBowelMovement delegates to bowelMovementDao`() = runTest {
        val entry = TestData.createBowelMovementEntry()
        coEvery { bowelMovementDao.insert(entry) } returns 1L

        val result = repository.insertBowelMovement(entry)

        assertThat(result).isEqualTo(1L)
        coVerify { bowelMovementDao.insert(entry) }
    }

    @Test
    fun `insertMedication delegates to medicationDao`() = runTest {
        val entry = TestData.createMedicationEntry()
        coEvery { medicationDao.insert(entry) } returns 1L

        val result = repository.insertMedication(entry)

        assertThat(result).isEqualTo(1L)
        coVerify { medicationDao.insert(entry) }
    }

    @Test
    fun `insertOtherEntry delegates to otherEntryDao`() = runTest {
        val entry = TestData.createOtherEntry()
        coEvery { otherEntryDao.insert(entry) } returns 1L

        val result = repository.insertOtherEntry(entry)

        assertThat(result).isEqualTo(1L)
        coVerify { otherEntryDao.insert(entry) }
    }

    // Update tests
    @Test
    fun `updateSymptom delegates to symptomEntryDao`() = runTest {
        val symptom = TestData.createSymptomEntry()

        repository.updateSymptom(symptom)

        coVerify { symptomEntryDao.update(symptom) }
    }

    @Test
    fun `updateBowelMovement delegates to bowelMovementDao`() = runTest {
        val entry = TestData.createBowelMovementEntry()

        repository.updateBowelMovement(entry)

        coVerify { bowelMovementDao.update(entry) }
    }

    @Test
    fun `updateMedication delegates to medicationDao`() = runTest {
        val entry = TestData.createMedicationEntry()

        repository.updateMedication(entry)

        coVerify { medicationDao.update(entry) }
    }

    @Test
    fun `updateOtherEntry delegates to otherEntryDao`() = runTest {
        val entry = TestData.createOtherEntry()

        repository.updateOtherEntry(entry)

        coVerify { otherEntryDao.update(entry) }
    }

    @Test
    fun `updateMeal delegates to mealDao`() = runTest {
        val meal = TestData.createMealEntry()
        val foods = listOf("Updated food")
        val tags = listOf("Updated tag")

        repository.updateMeal(meal, foods, tags)

        coVerify { mealDao.updateMealWithDetails(meal, foods, tags) }
    }

    // Delete tests
    @Test
    fun `deleteMeal delegates to mealDao`() = runTest {
        val meal = TestData.createMealEntry()

        repository.deleteMeal(meal)

        coVerify { mealDao.deleteMeal(meal) }
    }

    @Test
    fun `deleteSymptom delegates to symptomEntryDao`() = runTest {
        val symptom = TestData.createSymptomEntry()

        repository.deleteSymptom(symptom)

        coVerify { symptomEntryDao.delete(symptom) }
    }

    @Test
    fun `deleteBowelMovement delegates to bowelMovementDao`() = runTest {
        val entry = TestData.createBowelMovementEntry()

        repository.deleteBowelMovement(entry)

        coVerify { bowelMovementDao.delete(entry) }
    }

    @Test
    fun `deleteMedication delegates to medicationDao`() = runTest {
        val entry = TestData.createMedicationEntry()

        repository.deleteMedication(entry)

        coVerify { medicationDao.delete(entry) }
    }

    @Test
    fun `deleteOtherEntry delegates to otherEntryDao`() = runTest {
        val entry = TestData.createOtherEntry()

        repository.deleteOtherEntry(entry)

        coVerify { otherEntryDao.delete(entry) }
    }

    // Delete by ID tests
    @Test
    fun `deleteMealById delegates to mealDao`() = runTest {
        repository.deleteMealById(1L)

        coVerify { mealDao.deleteMealById(1L) }
    }

    @Test
    fun `deleteSymptomById delegates to symptomEntryDao`() = runTest {
        repository.deleteSymptomById(1L)

        coVerify { symptomEntryDao.deleteById(1L) }
    }

    @Test
    fun `deleteBowelMovementById delegates to bowelMovementDao`() = runTest {
        repository.deleteBowelMovementById(1L)

        coVerify { bowelMovementDao.deleteById(1L) }
    }

    // End symptom test
    @Test
    fun `endSymptom updates endTime via symptomEntryDao`() = runTest {
        val endTime = 5000L

        repository.endSymptom(1L, endTime)

        coVerify { symptomEntryDao.updateEndTime(1L, endTime) }
    }

    // Get by ID tests
    @Test
    fun `getSymptomById returns symptom from dao`() = runTest {
        val symptom = TestData.createSymptomEntry()
        coEvery { symptomEntryDao.getById(1L) } returns symptom

        val result = repository.getSymptomById(1L)

        assertThat(result).isEqualTo(symptom)
    }

    @Test
    fun `getSymptomById returns null when not found`() = runTest {
        coEvery { symptomEntryDao.getById(999L) } returns null

        val result = repository.getSymptomById(999L)

        assertThat(result).isNull()
    }

    @Test
    fun `getBowelMovementById returns entry from dao`() = runTest {
        val entry = TestData.createBowelMovementEntry()
        coEvery { bowelMovementDao.getById(1L) } returns entry

        val result = repository.getBowelMovementById(1L)

        assertThat(result).isEqualTo(entry)
    }

    @Test
    fun `getMedicationById returns entry from dao`() = runTest {
        val entry = TestData.createMedicationEntry()
        coEvery { medicationDao.getById(1L) } returns entry

        val result = repository.getMedicationById(1L)

        assertThat(result).isEqualTo(entry)
    }

    @Test
    fun `getOtherEntryById returns entry from dao`() = runTest {
        val entry = TestData.createOtherEntry()
        coEvery { otherEntryDao.getById(1L) } returns entry

        val result = repository.getOtherEntryById(1L)

        assertThat(result).isEqualTo(entry)
    }

    @Test
    fun `getMealWithDetailsById returns meal from dao`() = runTest {
        val mealWithDetails = TestData.createMealWithDetails()
        coEvery { mealDao.getMealWithDetailsById(1L) } returns mealWithDetails

        val result = repository.getMealWithDetailsById(1L)

        assertThat(result).isEqualTo(mealWithDetails)
    }

    // Flow tests
    @Test
    fun `getRecentMeals returns flow from dao`() = runTest {
        val meals = listOf(TestData.createMealWithDetails())
        every { mealDao.getRecentMealsWithDetails(5) } returns flowOf(meals)

        val flow = repository.getRecentMeals(5)

        flow.collect { result ->
            assertThat(result).isEqualTo(meals)
        }
    }

    @Test
    fun `getRecentSymptomEntries returns flow from dao`() = runTest {
        val symptoms = listOf(TestData.createSymptomEntry())
        every { symptomEntryDao.getRecentSymptomEntries(5) } returns flowOf(symptoms)

        val flow = repository.getRecentSymptomEntries(5)

        flow.collect { result ->
            assertThat(result).isEqualTo(symptoms)
        }
    }

    @Test
    fun `getRecentBowelMovements returns flow from dao`() = runTest {
        val entries = listOf(TestData.createBowelMovementEntry())
        every { bowelMovementDao.getRecentBowelMovements(5) } returns flowOf(entries)

        val flow = repository.getRecentBowelMovements(5)

        flow.collect { result ->
            assertThat(result).isEqualTo(entries)
        }
    }

    @Test
    fun `getRecentMedications returns flow from dao`() = runTest {
        val entries = listOf(TestData.createMedicationEntry())
        every { medicationDao.getRecentMedications(5) } returns flowOf(entries)

        val flow = repository.getRecentMedications(5)

        flow.collect { result ->
            assertThat(result).isEqualTo(entries)
        }
    }

    @Test
    fun `getRecentOtherEntries returns flow from dao`() = runTest {
        val entries = listOf(TestData.createOtherEntry())
        every { otherEntryDao.getRecentOtherEntries(5) } returns flowOf(entries)

        val flow = repository.getRecentOtherEntries(5)

        flow.collect { result ->
            assertThat(result).isEqualTo(entries)
        }
    }
}
