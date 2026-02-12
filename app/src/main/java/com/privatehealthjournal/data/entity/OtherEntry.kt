package com.privatehealthjournal.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class OtherEntryType {
    BOWEL_MOVEMENT,
    SLEEP,
    EXERCISE,
    STRESS,
    WATER_INTAKE,
    OTHER
}

@Entity(tableName = "other_entries")
data class OtherEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val entryType: OtherEntryType,
    val subType: String = "",
    val value: String = "",
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
