package com.privatehealthjournal.util

import com.privatehealthjournal.data.entity.BloodPressureEntry
import com.privatehealthjournal.data.entity.BowelMovementEntry
import com.privatehealthjournal.data.entity.CholesterolEntry
import com.privatehealthjournal.data.entity.FoodItem
import com.privatehealthjournal.data.entity.MealEntry
import com.privatehealthjournal.data.entity.MealType
import com.privatehealthjournal.data.entity.MealWithDetails
import com.privatehealthjournal.data.entity.MedicationEntry
import com.privatehealthjournal.data.entity.OtherEntry
import com.privatehealthjournal.data.entity.OtherEntryType
import com.privatehealthjournal.data.entity.SymptomEntry
import com.privatehealthjournal.data.entity.Tag
import com.privatehealthjournal.data.entity.WeightEntry
import com.privatehealthjournal.data.entity.WeightUnit

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

    fun createBloodPressureEntry(
        id: Long = 1L,
        systolic: Int = 120,
        diastolic: Int = 80,
        pulse: Int? = 72,
        notes: String = "Test notes",
        timestamp: Long = 1000L
    ) = BloodPressureEntry(
        id = id,
        systolic = systolic,
        diastolic = diastolic,
        pulse = pulse,
        notes = notes,
        timestamp = timestamp
    )

    fun createCholesterolEntry(
        id: Long = 1L,
        total: Int? = 200,
        ldl: Int? = 100,
        hdl: Int? = 60,
        triglycerides: Int? = 150,
        notes: String = "Test notes",
        timestamp: Long = 1000L
    ) = CholesterolEntry(
        id = id,
        total = total,
        ldl = ldl,
        hdl = hdl,
        triglycerides = triglycerides,
        notes = notes,
        timestamp = timestamp
    )

    fun createWeightEntry(
        id: Long = 1L,
        weight: Double = 150.0,
        unit: WeightUnit = WeightUnit.LB,
        notes: String = "Test notes",
        timestamp: Long = 1000L
    ) = WeightEntry(
        id = id,
        weight = weight,
        unit = unit,
        notes = notes,
        timestamp = timestamp
    )
}
