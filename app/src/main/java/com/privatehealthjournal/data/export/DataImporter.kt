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
import com.privatehealthjournal.data.entity.SymptomEntry
import com.privatehealthjournal.data.entity.WeightEntry
import com.privatehealthjournal.data.entity.WeightUnit
import com.privatehealthjournal.data.repository.LogRepository
import com.google.gson.Gson

object DataImporter {

    private val gson = Gson()

    suspend fun import(json: String, repository: LogRepository): ImportResult {
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

            // Import meals
            exportData.meals.forEach { meal ->
                val mealType = try {
                    MealType.valueOf(meal.mealType)
                } catch (e: IllegalArgumentException) {
                    MealType.SNACK
                }
                repository.insertMeal(
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
                repository.insertSymptom(
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
                repository.insertBowelMovement(
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
                repository.insertMedication(
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
                repository.insertOtherEntry(
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
                repository.insertBloodPressure(
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
                repository.insertCholesterol(
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
                repository.insertWeight(
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
                repository.insertSpO2(
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
                repository.insertBloodGlucose(
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
                val newSetId = repository.insertMedicationSet(exportedSet.name, items)
                medicationSetsImported++

                exportedSet.reminders.forEach { reminder ->
                    repository.insertReminder(
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
                    repository.insertMedicationSetLog(
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
                repository.insertCycleEntry(
                    CycleEntry(
                        flow = flow,
                        symptoms = entry.symptoms,
                        notes = entry.notes,
                        timestamp = entry.timestamp
                    )
                )
                cycleEntriesImported++
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
                cycleEntriesImported = cycleEntriesImported
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
        val cycleEntriesImported: Int = 0
    ) : ImportResult() {
        val totalImported: Int
            get() = mealsImported + symptomsImported + bowelMovementsImported +
                    medicationsImported + otherEntriesImported +
                    bloodPressureImported + cholesterolImported + weightImported + spO2Imported +
                    bloodGlucoseImported + medicationSetsImported +
                    medicationSetRemindersImported + medicationSetLogsImported +
                    cycleEntriesImported
    }

    data class Error(val message: String) : ImportResult()
}
