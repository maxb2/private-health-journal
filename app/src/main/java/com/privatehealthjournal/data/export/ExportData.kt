package com.privatehealthjournal.data.export

data class ExportData(
    val version: Int = 2,
    val exportedAt: Long = System.currentTimeMillis(),
    val meals: List<ExportedMeal> = emptyList(),
    val symptoms: List<ExportedSymptom> = emptyList(),
    val medications: List<ExportedMedication> = emptyList(),
    val otherEntries: List<ExportedOtherEntry> = emptyList(),
    val bloodPressureEntries: List<ExportedBloodPressure> = emptyList(),
    val cholesterolEntries: List<ExportedCholesterol> = emptyList(),
    val weightEntries: List<ExportedWeight> = emptyList()
)

data class ExportedMeal(
    val mealType: String,
    val notes: String,
    val timestamp: Long,
    val foods: List<String>,
    val tags: List<String>
)

data class ExportedSymptom(
    val name: String,
    val severity: Int,
    val notes: String,
    val startTime: Long,
    val endTime: Long?
)

data class ExportedMedication(
    val name: String,
    val dosage: String,
    val notes: String,
    val timestamp: Long
)

data class ExportedOtherEntry(
    val entryType: String,
    val subType: String,
    val value: String,
    val notes: String,
    val timestamp: Long
)

data class ExportedBloodPressure(
    val systolic: Int,
    val diastolic: Int,
    val pulse: Int?,
    val notes: String,
    val timestamp: Long
)

data class ExportedCholesterol(
    val total: Int?,
    val ldl: Int?,
    val hdl: Int?,
    val triglycerides: Int?,
    val notes: String,
    val timestamp: Long
)

data class ExportedWeight(
    val weight: Double,
    val unit: String,
    val notes: String,
    val timestamp: Long
)
