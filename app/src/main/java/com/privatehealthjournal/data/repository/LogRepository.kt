package com.privatehealthjournal.data.repository

import com.privatehealthjournal.data.dao.BloodGlucoseDao
import com.privatehealthjournal.data.dao.BloodPressureDao
import com.privatehealthjournal.data.dao.BowelMovementDao
import com.privatehealthjournal.data.dao.CholesterolDao
import com.privatehealthjournal.data.dao.MealDao
import com.privatehealthjournal.data.dao.MedicationDao
import com.privatehealthjournal.data.dao.MedicationSetDao
import com.privatehealthjournal.data.dao.OtherEntryDao
import com.privatehealthjournal.data.dao.SpO2Dao
import com.privatehealthjournal.data.dao.SymptomEntryDao
import com.privatehealthjournal.data.dao.WeightDao
import com.privatehealthjournal.data.entity.BloodGlucoseEntry
import com.privatehealthjournal.data.entity.BloodPressureEntry
import com.privatehealthjournal.data.entity.BowelMovementEntry
import com.privatehealthjournal.data.entity.CholesterolEntry
import com.privatehealthjournal.data.entity.MealEntry
import com.privatehealthjournal.data.entity.MealType
import com.privatehealthjournal.data.entity.MealWithDetails
import com.privatehealthjournal.data.entity.MedicationEntry
import com.privatehealthjournal.data.entity.MedicationSet
import com.privatehealthjournal.data.entity.MedicationSetWithItems
import com.privatehealthjournal.data.entity.OtherEntry
import com.privatehealthjournal.data.entity.OtherEntryType
import com.privatehealthjournal.data.entity.SpO2Entry
import com.privatehealthjournal.data.entity.SymptomEntry
import com.privatehealthjournal.data.entity.Tag
import com.privatehealthjournal.data.entity.WeightEntry
import kotlinx.coroutines.flow.Flow

class LogRepository(
    private val mealDao: MealDao,
    private val symptomEntryDao: SymptomEntryDao,
    private val bowelMovementDao: BowelMovementDao,
    private val medicationDao: MedicationDao,
    private val otherEntryDao: OtherEntryDao,
    private val bloodPressureDao: BloodPressureDao,
    private val cholesterolDao: CholesterolDao,
    private val weightDao: WeightDao,
    private val spO2Dao: SpO2Dao,
    private val bloodGlucoseDao: BloodGlucoseDao,
    private val medicationSetDao: MedicationSetDao
) {
    val allMeals: Flow<List<MealWithDetails>> = mealDao.getAllMealsWithDetails()
    val allSymptomEntries: Flow<List<SymptomEntry>> = symptomEntryDao.getAllSymptomEntries()
    val allBowelMovements: Flow<List<BowelMovementEntry>> = bowelMovementDao.getAllBowelMovements()
    val ongoingSymptoms: Flow<List<SymptomEntry>> = symptomEntryDao.getOngoingSymptoms()
    val allTags: Flow<List<Tag>> = mealDao.getAllTags()
    val allMedications: Flow<List<MedicationEntry>> = medicationDao.getAllMedications()
    val allMedicationNames: Flow<List<String>> = medicationDao.getAllMedicationNames()
    val allOtherEntries: Flow<List<OtherEntry>> = otherEntryDao.getAllOtherEntries()
    val allBloodPressureEntries: Flow<List<BloodPressureEntry>> = bloodPressureDao.getAllBloodPressureEntries()
    val allCholesterolEntries: Flow<List<CholesterolEntry>> = cholesterolDao.getAllCholesterolEntries()
    val allWeightEntries: Flow<List<WeightEntry>> = weightDao.getAllWeightEntries()
    val allSpO2Entries: Flow<List<SpO2Entry>> = spO2Dao.getAllSpO2Entries()
    val allBloodGlucoseEntries: Flow<List<BloodGlucoseEntry>> = bloodGlucoseDao.getAllBloodGlucoseEntries()
    val allMedicationSets: Flow<List<MedicationSetWithItems>> = medicationSetDao.getAllSetsWithItems()
    val allFoodNames: Flow<List<String>> = mealDao.getAllFoodNames()
    val allSymptomNames: Flow<List<String>> = symptomEntryDao.getAllSymptomNames()
    fun getDistinctOtherSubTypes(type: OtherEntryType): Flow<List<String>> =
        otherEntryDao.getDistinctSubTypes(type)

    fun getRecentMeals(limit: Int = 5): Flow<List<MealWithDetails>> {
        return mealDao.getRecentMealsWithDetails(limit)
    }

    fun getRecentSymptomEntries(limit: Int = 5): Flow<List<SymptomEntry>> {
        return symptomEntryDao.getRecentSymptomEntries(limit)
    }

    fun getRecentBowelMovements(limit: Int = 5): Flow<List<BowelMovementEntry>> {
        return bowelMovementDao.getRecentBowelMovements(limit)
    }

    fun getRecentMedications(limit: Int = 5): Flow<List<MedicationEntry>> {
        return medicationDao.getRecentMedications(limit)
    }

    fun getRecentOtherEntries(limit: Int = 5): Flow<List<OtherEntry>> {
        return otherEntryDao.getRecentOtherEntries(limit)
    }

    fun getRecentBloodPressureEntries(limit: Int = 5): Flow<List<BloodPressureEntry>> {
        return bloodPressureDao.getRecentBloodPressureEntries(limit)
    }

    fun getRecentCholesterolEntries(limit: Int = 5): Flow<List<CholesterolEntry>> {
        return cholesterolDao.getRecentCholesterolEntries(limit)
    }

    fun getRecentWeightEntries(limit: Int = 5): Flow<List<WeightEntry>> {
        return weightDao.getRecentWeightEntries(limit)
    }

    fun getRecentSpO2Entries(limit: Int = 5): Flow<List<SpO2Entry>> {
        return spO2Dao.getRecentSpO2Entries(limit)
    }

    fun getRecentBloodGlucoseEntries(limit: Int = 5): Flow<List<BloodGlucoseEntry>> {
        return bloodGlucoseDao.getRecentBloodGlucoseEntries(limit)
    }

    suspend fun insertMeal(
        mealType: MealType,
        foods: List<String>,
        tags: List<String>,
        notes: String = "",
        timestamp: Long = System.currentTimeMillis()
    ): Long {
        val meal = MealEntry(mealType = mealType, notes = notes, timestamp = timestamp)
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

    // Update methods
    suspend fun updateSymptom(symptomEntry: SymptomEntry) {
        symptomEntryDao.update(symptomEntry)
    }

    suspend fun updateBowelMovement(entry: BowelMovementEntry) {
        bowelMovementDao.update(entry)
    }

    suspend fun updateMeal(
        meal: MealEntry,
        foods: List<String>,
        tags: List<String>
    ) {
        mealDao.updateMealWithDetails(meal, foods, tags)
    }

    // Get by ID methods
    suspend fun getSymptomById(id: Long): SymptomEntry? {
        return symptomEntryDao.getById(id)
    }

    suspend fun getBowelMovementById(id: Long): BowelMovementEntry? {
        return bowelMovementDao.getById(id)
    }

    suspend fun getMealWithDetailsById(id: Long): MealWithDetails? {
        return mealDao.getMealWithDetailsById(id)
    }

    // Medication methods
    suspend fun insertMedication(entry: MedicationEntry): Long {
        return medicationDao.insert(entry)
    }

    suspend fun updateMedication(entry: MedicationEntry) {
        medicationDao.update(entry)
    }

    suspend fun deleteMedication(entry: MedicationEntry) {
        medicationDao.delete(entry)
    }

    suspend fun getMedicationById(id: Long): MedicationEntry? {
        return medicationDao.getById(id)
    }

    // Other entry methods
    suspend fun insertOtherEntry(entry: OtherEntry): Long {
        return otherEntryDao.insert(entry)
    }

    suspend fun updateOtherEntry(entry: OtherEntry) {
        otherEntryDao.update(entry)
    }

    suspend fun deleteOtherEntry(entry: OtherEntry) {
        otherEntryDao.delete(entry)
    }

    suspend fun getOtherEntryById(id: Long): OtherEntry? {
        return otherEntryDao.getById(id)
    }

    // Blood Pressure methods
    suspend fun insertBloodPressure(entry: BloodPressureEntry): Long {
        return bloodPressureDao.insert(entry)
    }

    suspend fun updateBloodPressure(entry: BloodPressureEntry) {
        bloodPressureDao.update(entry)
    }

    suspend fun deleteBloodPressure(entry: BloodPressureEntry) {
        bloodPressureDao.delete(entry)
    }

    suspend fun getBloodPressureById(id: Long): BloodPressureEntry? {
        return bloodPressureDao.getById(id)
    }

    // Cholesterol methods
    suspend fun insertCholesterol(entry: CholesterolEntry): Long {
        return cholesterolDao.insert(entry)
    }

    suspend fun updateCholesterol(entry: CholesterolEntry) {
        cholesterolDao.update(entry)
    }

    suspend fun deleteCholesterol(entry: CholesterolEntry) {
        cholesterolDao.delete(entry)
    }

    suspend fun getCholesterolById(id: Long): CholesterolEntry? {
        return cholesterolDao.getById(id)
    }

    // Weight methods
    suspend fun insertWeight(entry: WeightEntry): Long {
        return weightDao.insert(entry)
    }

    suspend fun updateWeight(entry: WeightEntry) {
        weightDao.update(entry)
    }

    suspend fun deleteWeight(entry: WeightEntry) {
        weightDao.delete(entry)
    }

    suspend fun getWeightById(id: Long): WeightEntry? {
        return weightDao.getById(id)
    }

    // SpO2 methods
    suspend fun insertSpO2(entry: SpO2Entry): Long {
        return spO2Dao.insert(entry)
    }

    suspend fun updateSpO2(entry: SpO2Entry) {
        spO2Dao.update(entry)
    }

    suspend fun deleteSpO2(entry: SpO2Entry) {
        spO2Dao.delete(entry)
    }

    suspend fun getSpO2ById(id: Long): SpO2Entry? {
        return spO2Dao.getById(id)
    }

    // Blood Glucose methods
    suspend fun insertBloodGlucose(entry: BloodGlucoseEntry): Long {
        return bloodGlucoseDao.insert(entry)
    }

    suspend fun updateBloodGlucose(entry: BloodGlucoseEntry) {
        bloodGlucoseDao.update(entry)
    }

    suspend fun deleteBloodGlucose(entry: BloodGlucoseEntry) {
        bloodGlucoseDao.delete(entry)
    }

    suspend fun getBloodGlucoseById(id: Long): BloodGlucoseEntry? {
        return bloodGlucoseDao.getById(id)
    }

    // Medication Set methods
    suspend fun insertMedicationSet(name: String, items: List<Pair<String, String>>): Long {
        return medicationSetDao.insertSetWithItems(MedicationSet(name = name), items)
    }

    suspend fun updateMedicationSet(set: MedicationSet, items: List<Pair<String, String>>) {
        medicationSetDao.updateSetWithItems(set, items)
    }

    suspend fun deleteMedicationSetById(id: Long) {
        medicationSetDao.deleteSetById(id)
    }

    suspend fun getMedicationSetWithItemsById(id: Long): MedicationSetWithItems? {
        return medicationSetDao.getSetWithItemsById(id)
    }
}
