package com.privatehealthjournal.data.export

import com.privatehealthjournal.data.entity.FlowIntensity
import com.privatehealthjournal.data.entity.GlucoseMealContext
import com.privatehealthjournal.data.entity.GlucoseUnit
import com.privatehealthjournal.data.entity.MealType
import com.privatehealthjournal.data.entity.OtherEntryType
import com.privatehealthjournal.data.entity.StepSource
import com.privatehealthjournal.data.entity.WeightUnit
import com.privatehealthjournal.data.repository.BiometricsRepository
import com.privatehealthjournal.data.repository.JournalRepository
import com.privatehealthjournal.data.repository.MedicationRepository
import com.privatehealthjournal.data.repository.StepsRepository
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DataImporterTest {

    private lateinit var journal: JournalRepository
    private lateinit var medication: MedicationRepository
    private lateinit var biometrics: BiometricsRepository
    private lateinit var steps: StepsRepository
    private lateinit var target: ImportTarget
    private val gson = Gson()

    @Before
    fun setup() {
        journal = mockk(relaxed = true)
        medication = mockk(relaxed = true)
        biometrics = mockk(relaxed = true)
        steps = mockk(relaxed = true)
        coEvery { journal.insertMeal(any(), any(), any(), any(), any()) } returns 1L
        coEvery { journal.insertSymptom(any()) } returns 1L
        coEvery { medication.insertMedication(any()) } returns 1L
        coEvery { journal.insertOtherEntry(any()) } returns 1L
        target = ImportTarget(journal, medication, biometrics, steps)
    }

    @Test
    fun `import empty data returns success with zero counts`() = runTest {
        val json = gson.toJson(ExportData())

        val result = DataImporter.import(json, target)

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

        val result = DataImporter.import(invalidJson, target)

        assertThat(result).isInstanceOf(ImportResult.Error::class.java)
    }

    @Test
    fun `import null data returns error`() = runTest {
        val json = "null"

        val result = DataImporter.import(json, target)

        assertThat(result).isInstanceOf(ImportResult.Error::class.java)
        assertThat((result as ImportResult.Error).message).contains("Invalid data format")
    }

    @Test
    fun `import meals calls journal repository`() = runTest {
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

        val result = DataImporter.import(json, target)

        assertThat(result).isInstanceOf(ImportResult.Success::class.java)
        assertThat((result as ImportResult.Success).mealsImported).isEqualTo(1)
        coVerify {
            journal.insertMeal(
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

        val result = DataImporter.import(json, target)

        assertThat(result).isInstanceOf(ImportResult.Success::class.java)
        coVerify {
            journal.insertMeal(
                mealType = MealType.SNACK,
                foods = any(),
                tags = any(),
                notes = any(),
                timestamp = any()
            )
        }
    }

    @Test
    fun `import symptoms calls journal repository`() = runTest {
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

        val result = DataImporter.import(json, target)

        assertThat(result).isInstanceOf(ImportResult.Success::class.java)
        assertThat((result as ImportResult.Success).symptomsImported).isEqualTo(1)
        coVerify {
            journal.insertSymptom(match {
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

        val result = DataImporter.import(json, target)

        assertThat(result).isInstanceOf(ImportResult.Success::class.java)
        coVerify {
            journal.insertSymptom(match { it.endTime == null })
        }
    }

    @Test
    fun `import medications calls medication repository`() = runTest {
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

        val result = DataImporter.import(json, target)

        assertThat(result).isInstanceOf(ImportResult.Success::class.java)
        assertThat((result as ImportResult.Success).medicationsImported).isEqualTo(1)
        coVerify {
            medication.insertMedication(match {
                it.name == "Aspirin" &&
                    it.dosage == "500mg" &&
                    it.notes == "For headache" &&
                    it.timestamp == 1000L
            })
        }
    }

    @Test
    fun `import other entries calls journal repository`() = runTest {
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

        val result = DataImporter.import(json, target)

        assertThat(result).isInstanceOf(ImportResult.Success::class.java)
        assertThat((result as ImportResult.Success).otherEntriesImported).isEqualTo(1)
        coVerify {
            journal.insertOtherEntry(match {
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

        val result = DataImporter.import(json, target)

        assertThat(result).isInstanceOf(ImportResult.Success::class.java)
        coVerify {
            journal.insertOtherEntry(match { it.entryType == OtherEntryType.OTHER })
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

        val result = DataImporter.import(json, target)

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
    fun `import bowel movements calls journal repository`() = runTest {
        val exportData = ExportData(
            bowelMovements = listOf(
                ExportedBowelMovement(bristolType = 4, notes = "normal", timestamp = 1000L)
            )
        )
        val json = gson.toJson(exportData)

        val result = DataImporter.import(json, target)

        assertThat(result).isInstanceOf(ImportResult.Success::class.java)
        assertThat((result as ImportResult.Success).bowelMovementsImported).isEqualTo(1)
        coVerify {
            journal.insertBowelMovement(match {
                it.bristolType == 4 && it.notes == "normal" && it.timestamp == 1000L
            })
        }
    }

    @Test
    fun `import blood pressure routes to biometrics repository`() = runTest {
        val exportData = ExportData(
            bloodPressureEntries = listOf(
                ExportedBloodPressure(systolic = 120, diastolic = 80, pulse = 70, notes = "n", timestamp = 1000L)
            )
        )
        val json = gson.toJson(exportData)

        val result = DataImporter.import(json, target)

        assertThat((result as ImportResult.Success).bloodPressureImported).isEqualTo(1)
        coVerify {
            biometrics.insertBloodPressure(match {
                it.systolic == 120 && it.diastolic == 80 && it.pulse == 70
            })
        }
    }

    @Test
    fun `import cholesterol routes to biometrics repository`() = runTest {
        val exportData = ExportData(
            cholesterolEntries = listOf(
                ExportedCholesterol(total = 180, ldl = 100, hdl = 50, triglycerides = 150, notes = "", timestamp = 1000L)
            )
        )
        val json = gson.toJson(exportData)

        val result = DataImporter.import(json, target)

        assertThat((result as ImportResult.Success).cholesterolImported).isEqualTo(1)
        coVerify {
            biometrics.insertCholesterol(match {
                it.total == 180 && it.ldl == 100 && it.hdl == 50 && it.triglycerides == 150
            })
        }
    }

    @Test
    fun `import weight routes to biometrics repository`() = runTest {
        val exportData = ExportData(
            weightEntries = listOf(
                ExportedWeight(weight = 150.5, unit = "KG", notes = "", timestamp = 1000L)
            )
        )
        val json = gson.toJson(exportData)

        val result = DataImporter.import(json, target)

        assertThat((result as ImportResult.Success).weightImported).isEqualTo(1)
        coVerify {
            biometrics.insertWeight(match {
                it.weight == 150.5 && it.unit == WeightUnit.KG
            })
        }
    }

    @Test
    fun `import invalid weight unit defaults to LB`() = runTest {
        val exportData = ExportData(
            weightEntries = listOf(
                ExportedWeight(weight = 150.0, unit = "INVALID", notes = "", timestamp = 1000L)
            )
        )
        val json = gson.toJson(exportData)

        DataImporter.import(json, target)

        coVerify { biometrics.insertWeight(match { it.unit == WeightUnit.LB }) }
    }

    @Test
    fun `import SpO2 routes to biometrics repository`() = runTest {
        val exportData = ExportData(
            spO2Entries = listOf(
                ExportedSpO2(spo2 = 98, pulse = 65, notes = "", timestamp = 1000L)
            )
        )
        val json = gson.toJson(exportData)

        val result = DataImporter.import(json, target)

        assertThat((result as ImportResult.Success).spO2Imported).isEqualTo(1)
        coVerify {
            biometrics.insertSpO2(match { it.spo2 == 98 && it.pulse == 65 })
        }
    }

    @Test
    fun `import blood glucose routes to biometrics repository`() = runTest {
        val exportData = ExportData(
            bloodGlucoseEntries = listOf(
                ExportedBloodGlucose(
                    glucoseLevel = 95.0, unit = "MMOL_L", mealContext = "FASTING",
                    notes = "", timestamp = 1000L
                )
            )
        )
        val json = gson.toJson(exportData)

        val result = DataImporter.import(json, target)

        assertThat((result as ImportResult.Success).bloodGlucoseImported).isEqualTo(1)
        coVerify {
            biometrics.insertBloodGlucose(match {
                it.glucoseLevel == 95.0 &&
                    it.unit == GlucoseUnit.MMOL_L &&
                    it.mealContext == GlucoseMealContext.FASTING
            })
        }
    }

    @Test
    fun `import blood glucose invalid unit and mealContext fall back`() = runTest {
        val exportData = ExportData(
            bloodGlucoseEntries = listOf(
                ExportedBloodGlucose(
                    glucoseLevel = 95.0, unit = "INVALID", mealContext = "INVALID",
                    notes = "", timestamp = 1000L
                )
            )
        )
        val json = gson.toJson(exportData)

        DataImporter.import(json, target)

        coVerify {
            biometrics.insertBloodGlucose(match {
                it.unit == GlucoseUnit.MG_DL && it.mealContext == null
            })
        }
    }

    @Test
    fun `import cycle entries route to journal repository`() = runTest {
        val exportData = ExportData(
            cycleEntries = listOf(
                ExportedCycleEntry(flow = "HEAVY", symptoms = "cramps", notes = "n", timestamp = 1000L)
            )
        )
        val json = gson.toJson(exportData)

        val result = DataImporter.import(json, target)

        assertThat((result as ImportResult.Success).cycleEntriesImported).isEqualTo(1)
        coVerify {
            journal.insertCycleEntry(match {
                it.flow == FlowIntensity.HEAVY && it.symptoms == "cramps"
            })
        }
    }

    @Test
    fun `import invalid cycle flow defaults to MEDIUM`() = runTest {
        val exportData = ExportData(
            cycleEntries = listOf(
                ExportedCycleEntry(flow = "INVALID", symptoms = "", notes = "", timestamp = 1000L)
            )
        )
        val json = gson.toJson(exportData)

        DataImporter.import(json, target)

        coVerify { journal.insertCycleEntry(match { it.flow == FlowIntensity.MEDIUM }) }
    }

    @Test
    fun `import step counts route to steps repository via upsert`() = runTest {
        val exportData = ExportData(
            stepCountEntries = listOf(
                ExportedStepCount(
                    dateEpochDay = 20_000L, steps = 8500, source = "HEALTH_CONNECT",
                    notes = "", timestamp = 1000L
                )
            )
        )
        val json = gson.toJson(exportData)

        val result = DataImporter.import(json, target)

        assertThat((result as ImportResult.Success).stepCountEntriesImported).isEqualTo(1)
        coVerify {
            steps.upsertStepCount(match {
                it.dateEpochDay == 20_000L &&
                    it.steps == 8500 &&
                    it.source == StepSource.HEALTH_CONNECT
            })
        }
    }

    @Test
    fun `import invalid step source defaults to MANUAL`() = runTest {
        val exportData = ExportData(
            stepCountEntries = listOf(
                ExportedStepCount(
                    dateEpochDay = 20_000L, steps = 8500, source = "INVALID",
                    notes = "", timestamp = 1000L
                )
            )
        )
        val json = gson.toJson(exportData)

        DataImporter.import(json, target)

        coVerify { steps.upsertStepCount(match { it.source == StepSource.MANUAL }) }
    }

    @Test
    fun `import medication sets routes set reminders and logs to medication repository`() = runTest {
        coEvery { medication.insertMedicationSet(any(), any()) } returns 42L

        val exportData = ExportData(
            medicationSets = listOf(
                ExportedMedicationSet(
                    name = "Evening",
                    items = listOf(
                        ExportedMedicationSetItem("Atorvastatin", "20mg"),
                        ExportedMedicationSetItem("Metformin", "500mg")
                    ),
                    reminders = listOf(
                        ExportedMedicationSetReminder(hour = 20, minute = 30, daysOfWeek = 127, enabled = true)
                    ),
                    logs = listOf(
                        ExportedMedicationSetLog(timestamp = 2000L)
                    )
                )
            )
        )
        val json = gson.toJson(exportData)

        val result = DataImporter.import(json, target) as ImportResult.Success

        assertThat(result.medicationSetsImported).isEqualTo(1)
        assertThat(result.medicationSetRemindersImported).isEqualTo(1)
        assertThat(result.medicationSetLogsImported).isEqualTo(1)
        coVerify {
            medication.insertMedicationSet(
                "Evening",
                listOf("Atorvastatin" to "20mg", "Metformin" to "500mg")
            )
            medication.insertReminder(match {
                it.setId == 42L && it.hour == 20 && it.minute == 30 && it.enabled
            })
            medication.insertMedicationSetLog(match {
                it.setId == 42L && it.timestamp == 2000L
            })
        }
    }

    @Test
    fun `meal import does not call other repositories`() = runTest {
        val exportData = ExportData(
            meals = listOf(
                ExportedMeal("BREAKFAST", "", 1000L, listOf("Eggs"), emptyList())
            )
        )
        val json = gson.toJson(exportData)

        DataImporter.import(json, target)

        coVerify(exactly = 0) { medication.insertMedication(any()) }
        coVerify(exactly = 0) { biometrics.insertBloodPressure(any()) }
        coVerify(exactly = 0) { steps.upsertStepCount(any()) }
    }

    @Test
    fun `import with repository exception returns error`() = runTest {
        coEvery { journal.insertMeal(any(), any(), any(), any(), any()) } throws RuntimeException("Database error")

        val exportData = ExportData(
            meals = listOf(
                ExportedMeal("BREAKFAST", "", 1000L, emptyList(), emptyList())
            )
        )
        val json = gson.toJson(exportData)

        val result = DataImporter.import(json, target)

        assertThat(result).isInstanceOf(ImportResult.Error::class.java)
        assertThat((result as ImportResult.Error).message).contains("Failed to import")
    }
}
