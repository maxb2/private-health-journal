package com.privatehealthjournal.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class BristolType(val type: Int, val displayName: String, val description: String) {
    TYPE_1(1, "Type 1", "Separate hard lumps, like nuts (hard to pass)"),
    TYPE_2(2, "Type 2", "Sausage-shaped, but lumpy"),
    TYPE_3(3, "Type 3", "Like a sausage but with cracks on surface"),
    TYPE_4(4, "Type 4", "Like a sausage or snake, smooth and soft"),
    TYPE_5(5, "Type 5", "Soft blobs with clear-cut edges (easy to pass)"),
    TYPE_6(6, "Type 6", "Fluffy pieces with ragged edges, mushy"),
    TYPE_7(7, "Type 7", "Watery, no solid pieces, entirely liquid");

    companion object {
        fun fromInt(value: Int): BristolType = entries.first { it.type == value }
    }
}

@Entity(tableName = "bowel_movement_entries")
data class BowelMovementEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bristolType: Int,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
