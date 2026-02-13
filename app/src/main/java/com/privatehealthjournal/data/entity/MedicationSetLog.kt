package com.privatehealthjournal.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medication_set_logs",
    foreignKeys = [
        ForeignKey(
            entity = MedicationSet::class,
            parentColumns = ["id"],
            childColumns = ["setId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("setId")]
)
data class MedicationSetLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val setId: Long,
    val timestamp: Long = System.currentTimeMillis()
)
