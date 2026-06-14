package com.privatehealthjournal.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "cholesterol_entries", indices = [Index("timestamp")])
data class CholesterolEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val total: Int? = null,
    val ldl: Int? = null,
    val hdl: Int? = null,
    val triglycerides: Int? = null,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
