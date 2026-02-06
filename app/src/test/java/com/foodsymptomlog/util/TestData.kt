package com.foodsymptomlog.util

import com.foodsymptomlog.data.entity.BowelMovementEntry
import com.foodsymptomlog.data.entity.FoodItem
import com.foodsymptomlog.data.entity.MealEntry
import com.foodsymptomlog.data.entity.MealType
import com.foodsymptomlog.data.entity.MealWithDetails
import com.foodsymptomlog.data.entity.MedicationEntry
import com.foodsymptomlog.data.entity.OtherEntry
import com.foodsymptomlog.data.entity.OtherEntryType
import com.foodsymptomlog.data.entity.SymptomEntry
import com.foodsymptomlog.data.entity.Tag

object TestData {

    fun createMealEntry(
        id: Long = 1L,
        mealType: MealType = MealType.BREAKFAST,
        notes: String = "Test notes",
        timestamp: Long = 1000L
    ) = MealEntry(
        id = id,
        mealType = mealType,
        notes = notes,
        timestamp = timestamp
    )

    fun createFoodItem(
        id: Long = 1L,
        mealId: Long = 1L,
        name: String = "Test Food"
    ) = FoodItem(
        id = id,
        mealId = mealId,
        name = name
    )

    fun createTag(
        id: Long = 1L,
        name: String = "Test Tag"
    ) = Tag(
        id = id,
        name = name
    )

    fun createMealWithDetails(
        meal: MealEntry = createMealEntry(),
        foods: List<FoodItem> = listOf(createFoodItem()),
        tags: List<Tag> = listOf(createTag())
    ) = MealWithDetails(
        meal = meal,
        foods = foods,
        tags = tags
    )

    fun createSymptomEntry(
        id: Long = 1L,
        name: String = "Headache",
        severity: Int = 3,
        notes: String = "Test notes",
        startTime: Long = 1000L,
        endTime: Long? = null
    ) = SymptomEntry(
        id = id,
        name = name,
        severity = severity,
        notes = notes,
        startTime = startTime,
        endTime = endTime
    )

    fun createBowelMovementEntry(
        id: Long = 1L,
        bristolType: Int = 4,
        notes: String = "Test notes",
        timestamp: Long = 1000L
    ) = BowelMovementEntry(
        id = id,
        bristolType = bristolType,
        notes = notes,
        timestamp = timestamp
    )

    fun createMedicationEntry(
        id: Long = 1L,
        name: String = "Ibuprofen",
        dosage: String = "200mg",
        notes: String = "Test notes",
        timestamp: Long = 1000L
    ) = MedicationEntry(
        id = id,
        name = name,
        dosage = dosage,
        notes = notes,
        timestamp = timestamp
    )

    fun createOtherEntry(
        id: Long = 1L,
        entryType: OtherEntryType = OtherEntryType.SLEEP,
        subType: String = "Night sleep",
        value: String = "8 hours",
        notes: String = "Test notes",
        timestamp: Long = 1000L
    ) = OtherEntry(
        id = id,
        entryType = entryType,
        subType = subType,
        value = value,
        notes = notes,
        timestamp = timestamp
    )
}
