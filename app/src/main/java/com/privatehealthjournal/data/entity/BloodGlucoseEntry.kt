package com.privatehealthjournal.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class GlucoseUnit(val displayLabel: String) {
    MG_DL("mg/dL"),
    MMOL_L("mmol/L")
}

enum class GlucoseMealContext(val displayLabel: String) {
    FASTING("Fasting"),
    BEFORE_MEAL("Before Meal"),
    AFTER_MEAL("After Meal"),
    BEDTIME("Bedtime")
}

@Entity(tableName = "blood_glucose_entries", indices = [Index("timestamp")])
data class BloodGlucoseEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val glucoseLevel: Double,
    val unit: GlucoseUnit = GlucoseUnit.MG_DL,
    val mealContext: GlucoseMealContext? = null,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
