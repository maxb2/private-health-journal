package com.privatehealthjournal.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "meal_tag_cross_ref",
    primaryKeys = ["mealId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = MealEntry::class,
            parentColumns = ["id"],
            childColumns = ["mealId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Tag::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("mealId"), Index("tagId")]
)
data class MealTagCrossRef(
    val mealId: Long,
    val tagId: Long
)
