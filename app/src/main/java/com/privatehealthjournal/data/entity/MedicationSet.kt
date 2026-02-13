package com.privatehealthjournal.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "medication_sets")
data class MedicationSet(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)

@Entity(
    tableName = "medication_set_items",
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
data class MedicationSetItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val setId: Long,
    val name: String,
    val dosage: String = ""
)

data class MedicationSetWithItems(
    @Embedded
    val set: MedicationSet,

    @Relation(
        parentColumn = "id",
        entityColumn = "setId"
    )
    val items: List<MedicationSetItem>
)
