package com.foodsymptomlog.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class WeightUnit {
    LB,
    KG
}

@Entity(tableName = "weight_entries")
data class WeightEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weight: Double,
    val unit: WeightUnit = WeightUnit.LB,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
