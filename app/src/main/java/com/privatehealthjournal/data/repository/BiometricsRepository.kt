package com.privatehealthjournal.data.repository

import com.privatehealthjournal.data.dao.BloodGlucoseDao
import com.privatehealthjournal.data.dao.BloodPressureDao
import com.privatehealthjournal.data.dao.CholesterolDao
import com.privatehealthjournal.data.dao.SpO2Dao
import com.privatehealthjournal.data.dao.WeightDao
import com.privatehealthjournal.data.entity.BloodGlucoseEntry
import com.privatehealthjournal.data.entity.BloodPressureEntry
import com.privatehealthjournal.data.entity.CholesterolEntry
import com.privatehealthjournal.data.entity.SpO2Entry
import com.privatehealthjournal.data.entity.WeightEntry
import kotlinx.coroutines.flow.Flow

class BiometricsRepository(
    private val bloodPressureDao: BloodPressureDao,
    private val cholesterolDao: CholesterolDao,
    private val weightDao: WeightDao,
    private val spO2Dao: SpO2Dao,
    private val bloodGlucoseDao: BloodGlucoseDao,
) {
    val allBloodPressureEntries: Flow<List<BloodPressureEntry>> = bloodPressureDao.getAllBloodPressureEntries()
    val allCholesterolEntries: Flow<List<CholesterolEntry>> = cholesterolDao.getAllCholesterolEntries()
    val allWeightEntries: Flow<List<WeightEntry>> = weightDao.getAllWeightEntries()
    val allSpO2Entries: Flow<List<SpO2Entry>> = spO2Dao.getAllSpO2Entries()
    val allBloodGlucoseEntries: Flow<List<BloodGlucoseEntry>> = bloodGlucoseDao.getAllBloodGlucoseEntries()

    fun getRecentBloodPressureEntries(limit: Int = 5): Flow<List<BloodPressureEntry>> =
        bloodPressureDao.getRecentBloodPressureEntries(limit)
    fun getRecentCholesterolEntries(limit: Int = 5): Flow<List<CholesterolEntry>> =
        cholesterolDao.getRecentCholesterolEntries(limit)
    fun getRecentWeightEntries(limit: Int = 5): Flow<List<WeightEntry>> =
        weightDao.getRecentWeightEntries(limit)
    fun getRecentSpO2Entries(limit: Int = 5): Flow<List<SpO2Entry>> =
        spO2Dao.getRecentSpO2Entries(limit)
    fun getRecentBloodGlucoseEntries(limit: Int = 5): Flow<List<BloodGlucoseEntry>> =
        bloodGlucoseDao.getRecentBloodGlucoseEntries(limit)

    // Blood Pressure
    suspend fun insertBloodPressure(entry: BloodPressureEntry): Long = bloodPressureDao.insert(entry)
    suspend fun updateBloodPressure(entry: BloodPressureEntry) = bloodPressureDao.update(entry)
    suspend fun deleteBloodPressure(entry: BloodPressureEntry) = bloodPressureDao.delete(entry)
    suspend fun getBloodPressureById(id: Long): BloodPressureEntry? = bloodPressureDao.getById(id)

    // Cholesterol
    suspend fun insertCholesterol(entry: CholesterolEntry): Long = cholesterolDao.insert(entry)
    suspend fun updateCholesterol(entry: CholesterolEntry) = cholesterolDao.update(entry)
    suspend fun deleteCholesterol(entry: CholesterolEntry) = cholesterolDao.delete(entry)
    suspend fun getCholesterolById(id: Long): CholesterolEntry? = cholesterolDao.getById(id)

    // Weight
    suspend fun insertWeight(entry: WeightEntry): Long = weightDao.insert(entry)
    suspend fun updateWeight(entry: WeightEntry) = weightDao.update(entry)
    suspend fun deleteWeight(entry: WeightEntry) = weightDao.delete(entry)
    suspend fun getWeightById(id: Long): WeightEntry? = weightDao.getById(id)

    // SpO2
    suspend fun insertSpO2(entry: SpO2Entry): Long = spO2Dao.insert(entry)
    suspend fun updateSpO2(entry: SpO2Entry) = spO2Dao.update(entry)
    suspend fun deleteSpO2(entry: SpO2Entry) = spO2Dao.delete(entry)
    suspend fun getSpO2ById(id: Long): SpO2Entry? = spO2Dao.getById(id)

    // Blood Glucose
    suspend fun insertBloodGlucose(entry: BloodGlucoseEntry): Long = bloodGlucoseDao.insert(entry)
    suspend fun updateBloodGlucose(entry: BloodGlucoseEntry) = bloodGlucoseDao.update(entry)
    suspend fun deleteBloodGlucose(entry: BloodGlucoseEntry) = bloodGlucoseDao.delete(entry)
    suspend fun getBloodGlucoseById(id: Long): BloodGlucoseEntry? = bloodGlucoseDao.getById(id)
}
