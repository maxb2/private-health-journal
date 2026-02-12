package com.privatehealthjournal.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "symptom_entries")
data class SymptomEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val severity: Int,
    val notes: String = "",
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null
) {
    val isOngoing: Boolean get() = endTime == null

    // For backward compatibility and sorting
    val timestamp: Long get() = startTime
}
