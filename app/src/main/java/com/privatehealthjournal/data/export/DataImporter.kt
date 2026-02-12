package com.privatehealthjournal.data.export

import com.privatehealthjournal.data.entity.BloodPressureEntry
import com.privatehealthjournal.data.entity.CholesterolEntry
import com.privatehealthjournal.data.entity.MealType
import com.privatehealthjournal.data.entity.MedicationEntry
import com.privatehealthjournal.data.entity.OtherEntry
import com.privatehealthjournal.data.entity.OtherEntryType
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
            var medicationsImported = 0
            var otherEntriesImported = 0
            var bloodPressureImported = 0
            var cholesterolImported = 0
            var weightImported = 0
            var spO2Imported = 0

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
                    timestamp = meal.timestamp
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
                        timestamp = other.timestamp
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

            ImportResult.Success(
                mealsImported = mealsImported,
                symptomsImported = symptomsImported,
                medicationsImported = medicationsImported,
                otherEntriesImported = otherEntriesImported,
                bloodPressureImported = bloodPressureImported,
                cholesterolImported = cholesterolImported,
                weightImported = weightImported,
                spO2Imported = spO2Imported
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
        val bloodPressureImported: Int = 0,
        val cholesterolImported: Int = 0,
        val weightImported: Int = 0,
        val spO2Imported: Int = 0
    ) : ImportResult() {
        val totalImported: Int
            get() = mealsImported + symptomsImported + medicationsImported + otherEntriesImported +
                    bloodPressureImported + cholesterolImported + weightImported + spO2Imported
    }

    data class Error(val message: String) : ImportResult()
}
