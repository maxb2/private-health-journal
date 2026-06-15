package com.privatehealthjournal.data.repository

import com.privatehealthjournal.data.dao.BowelMovementDao
import com.privatehealthjournal.data.dao.CycleEntryDao
import com.privatehealthjournal.data.dao.MealDao
import com.privatehealthjournal.data.dao.OtherEntryDao
import com.privatehealthjournal.data.dao.SymptomEntryDao
import com.privatehealthjournal.data.entity.BowelMovementEntry
import com.privatehealthjournal.data.entity.CycleEntry
import com.privatehealthjournal.data.entity.MealEntry
import com.privatehealthjournal.data.entity.MealType
import com.privatehealthjournal.data.entity.MealWithDetails
import com.privatehealthjournal.data.entity.OtherEntry
import com.privatehealthjournal.data.entity.OtherEntryType
import com.privatehealthjournal.data.entity.SymptomEntry
import com.privatehealthjournal.data.entity.Tag
import kotlinx.coroutines.flow.Flow

class JournalRepository(
    private val mealDao: MealDao,
    private val symptomEntryDao: SymptomEntryDao,
    private val bowelMovementDao: BowelMovementDao,
    private val otherEntryDao: OtherEntryDao,
    private val cycleEntryDao: CycleEntryDao,
) {
    // Meals
    val allMeals: Flow<List<MealWithDetails>> = mealDao.getAllMealsWithDetails()
    val allTags: Flow<List<Tag>> = mealDao.getAllTags()
    val allFoodNames: Flow<List<String>> = mealDao.getAllFoodNames()
    fun getRecentMeals(limit: Int = 5): Flow<List<MealWithDetails>> =
        mealDao.getRecentMealsWithDetails(limit)
    suspend fun insertMeal(
        mealType: MealType, foods: List<String>, tags: List<String>,
        notes: String = "", timestamp: Long = System.currentTimeMillis(), pointCost: Int? = null,
    ): Long {
        val meal = MealEntry(mealType = mealType, notes = notes, timestamp = timestamp, pointCost = pointCost)
        return mealDao.insertMealWithDetails(meal, foods, tags)
    }
    suspend fun updateMeal(meal: MealEntry, foods: List<String>, tags: List<String>) =
        mealDao.updateMealWithDetails(meal, foods, tags)
    suspend fun deleteMeal(meal: MealEntry) = mealDao.deleteMeal(meal)
    suspend fun getMealWithDetailsById(id: Long): MealWithDetails? = mealDao.getMealWithDetailsById(id)

    // Symptoms
    val allSymptomEntries: Flow<List<SymptomEntry>> = symptomEntryDao.getAllSymptomEntries()
    val ongoingSymptoms: Flow<List<SymptomEntry>> = symptomEntryDao.getOngoingSymptoms()
    val allSymptomNames: Flow<List<String>> = symptomEntryDao.getAllSymptomNames()
    fun getRecentSymptomEntries(limit: Int = 5): Flow<List<SymptomEntry>> =
        symptomEntryDao.getRecentSymptomEntries(limit)
    suspend fun insertSymptom(entry: SymptomEntry): Long = symptomEntryDao.insert(entry)
    suspend fun updateSymptom(entry: SymptomEntry) = symptomEntryDao.update(entry)
    suspend fun deleteSymptom(entry: SymptomEntry) = symptomEntryDao.delete(entry)
    suspend fun getSymptomById(id: Long): SymptomEntry? = symptomEntryDao.getById(id)
    suspend fun endSymptom(id: Long, endTime: Long = System.currentTimeMillis()) =
        symptomEntryDao.updateEndTime(id, endTime)

    // Bowel movements
    val allBowelMovements: Flow<List<BowelMovementEntry>> = bowelMovementDao.getAllBowelMovements()
    fun getRecentBowelMovements(limit: Int = 5): Flow<List<BowelMovementEntry>> =
        bowelMovementDao.getRecentBowelMovements(limit)
    suspend fun insertBowelMovement(entry: BowelMovementEntry): Long = bowelMovementDao.insert(entry)
    suspend fun updateBowelMovement(entry: BowelMovementEntry) = bowelMovementDao.update(entry)
    suspend fun deleteBowelMovement(entry: BowelMovementEntry) = bowelMovementDao.delete(entry)
    suspend fun getBowelMovementById(id: Long): BowelMovementEntry? = bowelMovementDao.getById(id)

    // Other entries
    val allOtherEntries: Flow<List<OtherEntry>> = otherEntryDao.getAllOtherEntries()
    fun getRecentOtherEntries(limit: Int = 5): Flow<List<OtherEntry>> =
        otherEntryDao.getRecentOtherEntries(limit)
    fun getDistinctOtherSubTypes(type: OtherEntryType): Flow<List<String>> =
        otherEntryDao.getDistinctSubTypes(type)
    suspend fun insertOtherEntry(entry: OtherEntry): Long = otherEntryDao.insert(entry)
    suspend fun updateOtherEntry(entry: OtherEntry) = otherEntryDao.update(entry)
    suspend fun deleteOtherEntry(entry: OtherEntry) = otherEntryDao.delete(entry)
    suspend fun getOtherEntryById(id: Long): OtherEntry? = otherEntryDao.getById(id)

    // Cycle
    val allCycleEntries: Flow<List<CycleEntry>> = cycleEntryDao.getAllCycleEntries()
    fun getRecentCycleEntries(limit: Int = 5): Flow<List<CycleEntry>> =
        cycleEntryDao.getRecentCycleEntries(limit)
    suspend fun insertCycleEntry(entry: CycleEntry): Long = cycleEntryDao.insert(entry)
    suspend fun updateCycleEntry(entry: CycleEntry) = cycleEntryDao.update(entry)
    suspend fun deleteCycleEntry(entry: CycleEntry) = cycleEntryDao.delete(entry)
    suspend fun getCycleEntryById(id: Long): CycleEntry? = cycleEntryDao.getById(id)
}
