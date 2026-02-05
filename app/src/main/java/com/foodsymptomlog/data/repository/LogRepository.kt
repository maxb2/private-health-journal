package com.foodsymptomlog.data.repository

import com.foodsymptomlog.data.dao.BowelMovementDao
import com.foodsymptomlog.data.dao.MealDao
import com.foodsymptomlog.data.dao.SymptomEntryDao
import com.foodsymptomlog.data.entity.BowelMovementEntry
import com.foodsymptomlog.data.entity.MealEntry
import com.foodsymptomlog.data.entity.MealType
import com.foodsymptomlog.data.entity.MealWithDetails
import com.foodsymptomlog.data.entity.SymptomEntry
import com.foodsymptomlog.data.entity.Tag
import kotlinx.coroutines.flow.Flow

class LogRepository(
    private val mealDao: MealDao,
    private val symptomEntryDao: SymptomEntryDao,
    private val bowelMovementDao: BowelMovementDao
) {
    val allMeals: Flow<List<MealWithDetails>> = mealDao.getAllMealsWithDetails()
    val allSymptomEntries: Flow<List<SymptomEntry>> = symptomEntryDao.getAllSymptomEntries()
    val allBowelMovements: Flow<List<BowelMovementEntry>> = bowelMovementDao.getAllBowelMovements()
    val ongoingSymptoms: Flow<List<SymptomEntry>> = symptomEntryDao.getOngoingSymptoms()
    val allTags: Flow<List<Tag>> = mealDao.getAllTags()

    fun getRecentMeals(limit: Int = 5): Flow<List<MealWithDetails>> {
        return mealDao.getRecentMealsWithDetails(limit)
    }

    fun getRecentSymptomEntries(limit: Int = 5): Flow<List<SymptomEntry>> {
        return symptomEntryDao.getRecentSymptomEntries(limit)
    }

    fun getRecentBowelMovements(limit: Int = 5): Flow<List<BowelMovementEntry>> {
        return bowelMovementDao.getRecentBowelMovements(limit)
    }

    suspend fun insertMeal(
        mealType: MealType,
        foods: List<String>,
        tags: List<String>,
        notes: String = ""
    ): Long {
        val meal = MealEntry(mealType = mealType, notes = notes)
        return mealDao.insertMealWithDetails(meal, foods, tags)
    }

    suspend fun insertSymptom(symptomEntry: SymptomEntry): Long {
        return symptomEntryDao.insert(symptomEntry)
    }

    suspend fun insertBowelMovement(entry: BowelMovementEntry): Long {
        return bowelMovementDao.insert(entry)
    }

    suspend fun deleteMeal(meal: MealEntry) {
        mealDao.deleteMeal(meal)
    }

    suspend fun deleteSymptom(symptomEntry: SymptomEntry) {
        symptomEntryDao.delete(symptomEntry)
    }

    suspend fun endSymptom(id: Long, endTime: Long = System.currentTimeMillis()) {
        symptomEntryDao.updateEndTime(id, endTime)
    }

    suspend fun deleteBowelMovement(entry: BowelMovementEntry) {
        bowelMovementDao.delete(entry)
    }

    suspend fun deleteMealById(id: Long) {
        mealDao.deleteMealById(id)
    }

    suspend fun deleteSymptomById(id: Long) {
        symptomEntryDao.deleteById(id)
    }

    suspend fun deleteBowelMovementById(id: Long) {
        bowelMovementDao.deleteById(id)
    }
}
