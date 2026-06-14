package com.privatehealthjournal.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class StepSource { SENSOR, HEALTH_CONNECT, MANUAL }

@Entity(
    tableName = "step_count_entries",
    indices = [Index(value = ["dateEpochDay"], unique = true)]
)
data class StepCountEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateEpochDay: Long,
    val steps: Int,
    val source: StepSource,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
