package com.privatehealthjournal.data.export

import com.privatehealthjournal.data.entity.BloodGlucoseEntry
import com.privatehealthjournal.data.entity.BloodPressureEntry
import com.privatehealthjournal.data.entity.BowelMovementEntry
import com.privatehealthjournal.data.entity.CholesterolEntry
import com.privatehealthjournal.data.entity.CycleEntry
import com.privatehealthjournal.data.entity.FlowIntensity
import com.privatehealthjournal.data.entity.MealType
import com.privatehealthjournal.data.entity.MedicationEntry
import com.privatehealthjournal.data.entity.MedicationSetLog
import com.privatehealthjournal.data.entity.MedicationSetReminder
import com.privatehealthjournal.data.entity.OtherEntry
import com.privatehealthjournal.data.entity.OtherEntryType
import com.privatehealthjournal.data.entity.GlucoseMealContext
import com.privatehealthjournal.data.entity.GlucoseUnit
import com.privatehealthjournal.data.entity.SpO2Entry
import com.privatehealthjournal.data.entity.StepCountEntry
import com.privatehealthjournal.data.entity.StepSource
import com.privatehealthjournal.data.entity.SymptomEntry
import com.privatehealthjournal.data.entity.WeightEntry
import com.privatehealthjournal.data.entity.WeightUnit
import com.privatehealthjournal.data.repository.BiometricsRepository
import com.privatehealthjournal.data.repository.JournalRepository
import com.privatehealthjournal.data.repository.MedicationRepository
import com.privatehealthjournal.data.repository.StepsRepository
import com.google.gson.Gson

class ImportTarget(
    val journal: JournalRepository,
    val medication: MedicationRepository,
    val biometrics: BiometricsRepository,
    val steps: StepsRepository,
)

object DataImporter {

    private val gson = Gson()

    suspend fun import(json: String, target: ImportTarget): ImportResult {
        return try {
            val exportData = gson.fromJson(json, ExportData::class.java)
                ?: return ImportResult.Error("Invalid data format")

            var mealsImported = 0
            var symptomsImported = 0
            var bowelMovementsImported = 0
            var medicationsImported = 0
            var otherEntriesImported = 0
            var bloodPressureImported = 0
            var cholesterolImported = 0
            var weightImported = 0
            var spO2Imported = 0
            var bloodGlucoseImported = 0
            var medicationSetsImported = 0
            var medicationSetRemindersImported = 0
            var medicationSetLogsImported = 0
            var cycleEntriesImported = 0
            var stepCountEntriesImported = 0

            // Import meals
            exportData.meals.forEach { meal ->
                val mealType = try {
                    MealType.valueOf(meal.mealType)
                } catch (e: IllegalArgumentException) {
                    MealType.SNACK
                }
                target.journal.insertMeal(
                    mealType = mealType,
                    foods = meal.foods,
                    tags = meal.tags,
                    notes = meal.notes,
                    timestamp = meal.timestamp,
                    pointCost = meal.pointCost
                )
                mealsImported++
            }

            // Import symptoms
            exportData.symptoms.forEach { symptom ->
                target.journal.insertSymptom(
                    SymptomEntry(
                        name = symptom.name,
                        severity = symptom.severity,
                        notes = symptom.notes,
                        startTime = symptom.startTime,
                        endTime = symptom.endTime
                    )
                )
                symptomsImported++
            }

            // Import bowel movements
            exportData.bowelMovements.forEach { bm ->
                target.journal.insertBowelMovement(
                    BowelMovementEntry(
                        bristolType = bm.bristolType,
                        notes = bm.notes,
                        timestamp = bm.timestamp
                    )
                )
                bowelMovementsImported++
            }

            // Import medications
            exportData.medications.forEach { med ->
                target.medication.insertMedication(
                    MedicationEntry(
                        name = med.name,
                        dosage = med.dosage,
                        notes = med.notes,
                        timestamp = med.timestamp
                    )
                )
                medicationsImported++
            }

            // Import other entries
            exportData.otherEntries.forEach { other ->
                val entryType = try {
                    OtherEntryType.valueOf(other.entryType)
                } catch (e: IllegalArgumentException) {
                    OtherEntryType.OTHER
                }
                target.journal.insertOtherEntry(
                    OtherEntry(
                        entryType = entryType,
                        subType = other.subType,
                        value = other.value,
                        notes = other.notes,
                        timestamp = other.timestamp,
                        pointCredit = other.pointCredit
                    )
                )
                otherEntriesImported++
            }

            // Import blood pressure entries
            exportData.bloodPressureEntries.forEach { bp ->
                target.biometrics.insertBloodPressure(
                    BloodPressureEntry(
                        systolic = bp.systolic,
                        diastolic = bp.diastolic,
                        pulse = bp.pulse,
                        notes = bp.notes,
                        timestamp = bp.timestamp
                    )
                )
                bloodPressureImported++
            }

            // Import cholesterol entries
            exportData.cholesterolEntries.forEach { chol ->
                target.biometrics.insertCholesterol(
                    CholesterolEntry(
                        total = chol.total,
                        ldl = chol.ldl,
                        hdl = chol.hdl,
                        triglycerides = chol.triglycerides,
                        notes = chol.notes,
                        timestamp = chol.timestamp
                    )
                )
                cholesterolImported++
            }

            // Import weight entries
            exportData.weightEntries.forEach { weight ->
                val unit = try {
                    WeightUnit.valueOf(weight.unit)
                } catch (e: IllegalArgumentException) {
                    WeightUnit.LB
                }
                target.biometrics.insertWeight(
                    WeightEntry(
                        weight = weight.weight,
                        unit = unit,
                        notes = weight.notes,
                        timestamp = weight.timestamp
                    )
                )
                weightImported++
            }

            // Import SpO2 entries
            exportData.spO2Entries.forEach { spo2 ->
                target.biometrics.insertSpO2(
                    SpO2Entry(
                        spo2 = spo2.spo2,
                        pulse = spo2.pulse,
                        notes = spo2.notes,
                        timestamp = spo2.timestamp
                    )
                )
                spO2Imported++
            }

            // Import blood glucose entries
            exportData.bloodGlucoseEntries.forEach { bg ->
                val unit = try {
                    GlucoseUnit.valueOf(bg.unit)
                } catch (e: IllegalArgumentException) {
                    GlucoseUnit.MG_DL
                }
                val mealContext = bg.mealContext?.let {
                    try {
                        GlucoseMealContext.valueOf(it)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }
                target.biometrics.insertBloodGlucose(
                    BloodGlucoseEntry(
                        glucoseLevel = bg.glucoseLevel,
                        unit = unit,
                        mealContext = mealContext,
                        notes = bg.notes,
                        timestamp = bg.timestamp
                    )
                )
                bloodGlucoseImported++
            }

            // Import medication sets (with nested reminders + logs)
            exportData.medicationSets.forEach { exportedSet ->
                val items = exportedSet.items.map { it.name to it.dosage }
                val newSetId = target.medication.insertMedicationSet(exportedSet.name, items)
                medicationSetsImported++

                exportedSet.reminders.forEach { reminder ->
                    target.medication.insertReminder(
                        MedicationSetReminder(
                            setId = newSetId,
                            hour = reminder.hour,
                            minute = reminder.minute,
                            daysOfWeek = reminder.daysOfWeek,
                            enabled = reminder.enabled
                        )
                    )
                    medicationSetRemindersImported++
                }

                exportedSet.logs.forEach { log ->
                    target.medication.insertMedicationSetLog(
                        MedicationSetLog(setId = newSetId, timestamp = log.timestamp)
                    )
                    medicationSetLogsImported++
                }
            }

            // Import cycle entries
            exportData.cycleEntries.forEach { entry ->
                val flow = try {
                    FlowIntensity.valueOf(entry.flow)
                } catch (e: IllegalArgumentException) {
                    FlowIntensity.MEDIUM
                }
                target.journal.insertCycleEntry(
                    CycleEntry(
                        flow = flow,
                        symptoms = entry.symptoms,
                        notes = entry.notes,
                        timestamp = entry.timestamp
                    )
                )
                cycleEntriesImported++
            }

            // Import step count entries (use upsert — restoring backup is trust-source, bypass merge)
            exportData.stepCountEntries.forEach { entry ->
                val source = try {
                    StepSource.valueOf(entry.source)
                } catch (e: IllegalArgumentException) {
                    StepSource.MANUAL
                }
                target.steps.upsertStepCount(
                    StepCountEntry(
                        dateEpochDay = entry.dateEpochDay,
                        steps = entry.steps,
                        source = source,
                        notes = entry.notes,
                        timestamp = entry.timestamp
                    )
                )
                stepCountEntriesImported++
            }

            ImportResult.Success(
                mealsImported = mealsImported,
                symptomsImported = symptomsImported,
                bowelMovementsImported = bowelMovementsImported,
                medicationsImported = medicationsImported,
                otherEntriesImported = otherEntriesImported,
                bloodPressureImported = bloodPressureImported,
                cholesterolImported = cholesterolImported,
                weightImported = weightImported,
                spO2Imported = spO2Imported,
                bloodGlucoseImported = bloodGlucoseImported,
                medicationSetsImported = medicationSetsImported,
                medicationSetRemindersImported = medicationSetRemindersImported,
                medicationSetLogsImported = medicationSetLogsImported,
                cycleEntriesImported = cycleEntriesImported,
                stepCountEntriesImported = stepCountEntriesImported
            )
        } catch (e: Exception) {
            ImportResult.Error("Failed to import: ${e.message}")
        }
    }
}

sealed class ImportResult {
    data class Success(
        val mealsImported: Int,
        val symptomsImported: Int,
        val medicationsImported: Int,
        val otherEntriesImported: Int,
        val bowelMovementsImported: Int = 0,
        val bloodPressureImported: Int = 0,
        val cholesterolImported: Int = 0,
        val weightImported: Int = 0,
        val spO2Imported: Int = 0,
        val bloodGlucoseImported: Int = 0,
        val medicationSetsImported: Int = 0,
        val medicationSetRemindersImported: Int = 0,
        val medicationSetLogsImported: Int = 0,
        val cycleEntriesImported: Int = 0,
        val stepCountEntriesImported: Int = 0
    ) : ImportResult() {
        val totalImported: Int
            get() = mealsImported + symptomsImported + bowelMovementsImported +
                    medicationsImported + otherEntriesImported +
                    bloodPressureImported + cholesterolImported + weightImported + spO2Imported +
                    bloodGlucoseImported + medicationSetsImported +
                    medicationSetRemindersImported + medicationSetLogsImported +
                    cycleEntriesImported + stepCountEntriesImported
    }

    data class Error(val message: String) : ImportResult()
}
