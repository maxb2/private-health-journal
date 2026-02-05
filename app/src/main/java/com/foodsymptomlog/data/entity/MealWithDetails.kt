package com.foodsymptomlog.data.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class MealWithDetails(
    @Embedded
    val meal: MealEntry,

    @Relation(
        parentColumn = "id",
        entityColumn = "mealId"
    )
    val foods: List<FoodItem>,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = MealTagCrossRef::class,
            parentColumn = "mealId",
            entityColumn = "tagId"
        )
    )
    val tags: List<Tag>
)
