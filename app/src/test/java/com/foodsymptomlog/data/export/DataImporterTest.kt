package com.foodsymptomlog.data.export

import com.foodsymptomlog.data.entity.MealType
import com.foodsymptomlog.data.entity.OtherEntryType
import com.foodsymptomlog.data.repository.LogRepository
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DataImporterTest {

    private lateinit var repository: LogRepository
    private val gson = Gson()

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        coEvery { repository.insertMeal(any(), any(), any(), any(), any()) } returns 1L
        coEvery { repository.insertSymptom(any()) } returns 1L
        coEvery { repository.insertMedication(any()) } returns 1L
        coEvery { repository.insertOtherEntry(any()) } returns 1L
    }

    @Test
    fun `import empty data returns success with zero counts`() = runTest {
        val json = gson.toJson(ExportData())

        val result = DataImporter.import(json, repository)

        assertThat(result).isInstanceOf(ImportResult.Success::class.java)
        val success = result as ImportResult.Success
        assertThat(success.mealsImported).isEqualTo(0)
        assertThat(success.symptomsImported).isEqualTo(0)
        assertThat(success.medicationsImported).isEqualTo(0)
        assertThat(success.otherEntriesImported).isEqualTo(0)
        assertThat(success.totalImported).isEqualTo(0)
    }

    @Test
    fun `import invalid JSON returns error`() = runTest {
        val invalidJson = "not valid json"

        val result = DataImporter.import(invalidJson, repository)

        assertThat(result).isInstanceOf(ImportResult.Error::class.java)
    }

    @Test
    fun `import null data returns error`() = runTest {
        val json = "null"

        val result = DataImporter.import(json, repository)

        assertThat(result).isInstanceOf(ImportResult.Error::class.java)
        assertThat((result as ImportResult.Error).message).contains("Invalid data format")
    }

    @Test
    fun `import meals calls repository`() = runTest {
        val exportData = ExportData(
            meals = listOf(
                ExportedMeal(
                    mealType = "BREAKFAST",
                    notes = "Test",
                    timestamp = 1000L,
                    foods = listOf("Eggs", "Toast"),
                    tags = listOf("Healthy")
                )
            )
        )
        val json = gson.toJson(exportData)

        val result = DataImporter.import(json, repository)

        assertThat(result).isInstanceOf(ImportResult.Success::class.java)
        assertThat((result as ImportResult.Success).mealsImported).isEqualTo(1)
        coVerify {
            repository.insertMeal(
                mealType = MealType.BREAKFAST,
                foods = listOf("Eggs", "Toast"),
                tags = listOf("Healthy"),
                notes = "Test",
                timestamp = 1000L
            )
        }
    }

    @Test
    fun `import invalid meal type defaults to SNACK`() = runTest {
        val exportData = ExportData(
            meals = listOf(
                ExportedMeal(
                    mealType = "INVALID_TYPE",
                    notes = "",
                    timestamp = 1000L,
                    foods = emptyList(),
                    tags = emptyList()
                )
            )
        )
        val json = gson.toJson(exportData)

        val result = DataImporter.import(json, repository)

        assertThat(result).isInstanceOf(ImportResult.Success::class.java)
        coVerify {
            repository.insertMeal(
                mealType = MealType.SNACK,
                foods = any(),
                tags = any(),
                notes = any(),
                timestamp = any()
            )
        }
    }

    @Test
    fun `import symptoms calls repository`() = runTest {
        val exportData = ExportData(
            symptoms = listOf(
                ExportedSymptom(
                    name = "Headache",
                    severity = 3,
                    notes = "After lunch",
                    startTime = 1000L,
                    endTime = 2000L
                )
            )
        )
        val json = gson.toJson(exportData)

        val result = DataImporter.import(json, repository)

        assertThat(result).isInstanceOf(ImportResult.Success::class.java)
        assertThat((result as ImportResult.Success).symptomsImported).isEqualTo(1)
        coVerify {
            repository.insertSymptom(match {
                it.name == "Headache" &&
                    it.severity == 3 &&
                    it.notes == "After lunch" &&
                    it.startTime == 1000L &&
                    it.endTime == 2000L
            })
        }
    }

    @Test
    fun `import ongoing symptom with null endTime`() = runTest {
        val exportData = ExportData(
            symptoms = listOf(
                ExportedSymptom(
                    name = "Fatigue",
                    severity = 2,
                    notes = "",
                    startTime = 1000L,
                    endTime = null
                )
            )
        )
        val json = gson.toJson(exportData)

        val result = DataImporter.import(json, repository)

        assertThat(result).isInstanceOf(ImportResult.Success::class.java)
        coVerify {
            repository.insertSymptom(match { it.endTime == null })
        }
    }

    @Test
    fun `import medications calls repository`() = runTest {
        val exportData = ExportData(
            medications = listOf(
                ExportedMedication(
                    name = "Aspirin",
                    dosage = "500mg",
                    notes = "For headache",
                    timestamp = 1000L
                )
            )
        )
        val json = gson.toJson(exportData)

        val result = DataImporter.import(json, repository)

        assertThat(result).isInstanceOf(ImportResult.Success::class.java)
        assertThat((result as ImportResult.Success).medicationsImported).isEqualTo(1)
        coVerify {
            repository.insertMedication(match {
                it.name == "Aspirin" &&
                    it.dosage == "500mg" &&
                    it.notes == "For headache" &&
                    it.timestamp == 1000L
            })
        }
    }

    @Test
    fun `import other entries calls repository`() = runTest {
        val exportData = ExportData(
            otherEntries = listOf(
                ExportedOtherEntry(
                    entryType = "SLEEP",
                    subType = "Night sleep",
                    value = "8 hours",
                    notes = "Good sleep",
                    timestamp = 1000L
                )
            )
        )
        val json = gson.toJson(exportData)

        val result = DataImporter.import(json, repository)

        assertThat(result).isInstanceOf(ImportResult.Success::class.java)
        assertThat((result as ImportResult.Success).otherEntriesImported).isEqualTo(1)
        coVerify {
            repository.insertOtherEntry(match {
                it.entryType == OtherEntryType.SLEEP &&
                    it.subType == "Night sleep" &&
                    it.value == "8 hours" &&
                    it.notes == "Good sleep" &&
                    it.timestamp == 1000L
            })
        }
    }

    @Test
    fun `import invalid other entry type defaults to OTHER`() = runTest {
        val exportData = ExportData(
            otherEntries = listOf(
                ExportedOtherEntry(
                    entryType = "INVALID_TYPE",
                    subType = "",
                    value = "",
                    notes = "",
                    timestamp = 1000L
                )
            )
        )
        val json = gson.toJson(exportData)

        val result = DataImporter.import(json, repository)

        assertThat(result).isInstanceOf(ImportResult.Success::class.java)
        coVerify {
            repository.insertOtherEntry(match { it.entryType == OtherEntryType.OTHER })
        }
    }

    @Test
    fun `import multiple entries of all types`() = runTest {
        val exportData = ExportData(
            meals = listOf(
                ExportedMeal("BREAKFAST", "", 1000L, listOf("Food1"), emptyList()),
                ExportedMeal("LUNCH", "", 2000L, listOf("Food2"), emptyList())
            ),
            symptoms = listOf(
                ExportedSymptom("Headache", 3, "", 1000L, null),
                ExportedSymptom("Nausea", 2, "", 2000L, 3000L)
            ),
            medications = listOf(
                ExportedMedication("Med1", "100mg", "", 1000L)
            ),
            otherEntries = listOf(
                ExportedOtherEntry("SLEEP", "", "8 hours", "", 1000L),
                ExportedOtherEntry("EXERCISE", "Walking", "30 min", "", 2000L)
            )
        )
        val json = gson.toJson(exportData)

        val result = DataImporter.import(json, repository)

        assertThat(result).isInstanceOf(ImportResult.Success::class.java)
        val success = result as ImportResult.Success
        assertThat(success.mealsImported).isEqualTo(2)
        assertThat(success.symptomsImported).isEqualTo(2)
        assertThat(success.medicationsImported).isEqualTo(1)
        assertThat(success.otherEntriesImported).isEqualTo(2)
        assertThat(success.totalImported).isEqualTo(7)
    }

    @Test
    fun `ImportResult Success totalImported is sum of all imports`() {
        val success = ImportResult.Success(
            mealsImported = 5,
            symptomsImported = 3,
            medicationsImported = 2,
            otherEntriesImported = 4
        )

        assertThat(success.totalImported).isEqualTo(14)
    }

    @Test
    fun `import with repository exception returns error`() = runTest {
        coEvery { repository.insertMeal(any(), any(), any(), any(), any()) } throws RuntimeException("Database error")

        val exportData = ExportData(
            meals = listOf(
                ExportedMeal("BREAKFAST", "", 1000L, emptyList(), emptyList())
            )
        )
        val json = gson.toJson(exportData)

        val result = DataImporter.import(json, repository)

        assertThat(result).isInstanceOf(ImportResult.Error::class.java)
        assertThat((result as ImportResult.Error).message).contains("Failed to import")
    }
}
