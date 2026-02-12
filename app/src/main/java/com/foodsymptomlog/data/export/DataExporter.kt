package com.foodsymptomlog.data.export

import com.foodsymptomlog.data.entity.BloodPressureEntry
import com.foodsymptomlog.data.entity.CholesterolEntry
import com.foodsymptomlog.data.entity.MealWithDetails
import com.foodsymptomlog.data.entity.MedicationEntry
import com.foodsymptomlog.data.entity.OtherEntry
import com.foodsymptomlog.data.entity.SymptomEntry
import com.foodsymptomlog.data.entity.WeightEntry
import com.google.gson.GsonBuilder

object DataExporter {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun export(
        meals: List<MealWithDetails>,
        symptoms: List<SymptomEntry>,
        medications: List<MedicationEntry>,
        otherEntries: List<OtherEntry>,
        bloodPressureEntries: List<BloodPressureEntry> = emptyList(),
        cholesterolEntries: List<CholesterolEntry> = emptyList(),
        weightEntries: List<WeightEntry> = emptyList()
    ): String {
        val exportData = ExportData(
            meals = meals.map { meal ->
                ExportedMeal(
                    mealType = meal.meal.mealType.name,
                    notes = meal.meal.notes,
                    timestamp = meal.meal.timestamp,
                    foods = meal.foods.map { it.name },
                    tags = meal.tags.map { it.name }
                )
            },
            symptoms = symptoms.map { symptom ->
                ExportedSymptom(
                    name = symptom.name,
                    severity = symptom.severity,
                    notes = symptom.notes,
                    startTime = symptom.startTime,
                    endTime = symptom.endTime
                )
            },
            medications = medications.map { med ->
                ExportedMedication(
                    name = med.name,
                    dosage = med.dosage,
                    notes = med.notes,
                    timestamp = med.timestamp
                )
            },
            otherEntries = otherEntries.map { other ->
                ExportedOtherEntry(
                    entryType = other.entryType.name,
                    subType = other.subType,
                    value = other.value,
                    notes = other.notes,
                    timestamp = other.timestamp
                )
            },
            bloodPressureEntries = bloodPressureEntries.map { bp ->
                ExportedBloodPressure(
                    systolic = bp.systolic,
                    diastolic = bp.diastolic,
                    pulse = bp.pulse,
                    notes = bp.notes,
                    timestamp = bp.timestamp
                )
            },
            cholesterolEntries = cholesterolEntries.map { chol ->
                ExportedCholesterol(
                    total = chol.total,
                    ldl = chol.ldl,
                    hdl = chol.hdl,
                    triglycerides = chol.triglycerides,
                    notes = chol.notes,
                    timestamp = chol.timestamp
                )
            },
            weightEntries = weightEntries.map { weight ->
                ExportedWeight(
                    weight = weight.weight,
                    unit = weight.unit.name,
                    notes = weight.notes,
                    timestamp = weight.timestamp
                )
            }
        )

        return gson.toJson(exportData)
    }
}
