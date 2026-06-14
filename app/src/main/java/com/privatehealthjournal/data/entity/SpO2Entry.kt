package com.privatehealthjournal.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "spo2_entries", indices = [Index("timestamp")])
data class SpO2Entry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val spo2: Int,
    val pulse: Int? = null,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
