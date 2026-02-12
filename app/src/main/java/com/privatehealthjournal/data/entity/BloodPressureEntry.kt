package com.privatehealthjournal.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blood_pressure_entries")
data class BloodPressureEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val systolic: Int,
    val diastolic: Int,
    val pulse: Int? = null,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
