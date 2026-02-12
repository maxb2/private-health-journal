package com.privatehealthjournal.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medication_entries")
data class MedicationEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val dosage: String = "",
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
