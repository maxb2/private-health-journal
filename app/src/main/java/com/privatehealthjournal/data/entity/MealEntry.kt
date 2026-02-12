package com.privatehealthjournal.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MealType {
    BREAKFAST,
    LUNCH,
    DINNER,
    SNACK
}

@Entity(tableName = "meal_entries")
data class MealEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mealType: MealType,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
