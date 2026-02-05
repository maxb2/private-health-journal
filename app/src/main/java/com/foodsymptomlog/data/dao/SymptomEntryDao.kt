package com.foodsymptomlog.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.foodsymptomlog.data.entity.SymptomEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface SymptomEntryDao {
    @Query("SELECT * FROM symptom_entries ORDER BY startTime DESC")
    fun getAllSymptomEntries(): Flow<List<SymptomEntry>>

    @Query("SELECT * FROM symptom_entries ORDER BY startTime DESC LIMIT :limit")
    fun getRecentSymptomEntries(limit: Int): Flow<List<SymptomEntry>>

    @Query("SELECT * FROM symptom_entries WHERE endTime IS NULL ORDER BY startTime DESC")
    fun getOngoingSymptoms(): Flow<List<SymptomEntry>>

    @Insert
    suspend fun insert(symptomEntry: SymptomEntry): Long

    @Query("UPDATE symptom_entries SET endTime = :endTime WHERE id = :id")
    suspend fun updateEndTime(id: Long, endTime: Long)

    @Delete
    suspend fun delete(symptomEntry: SymptomEntry)

    @Query("DELETE FROM symptom_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}
