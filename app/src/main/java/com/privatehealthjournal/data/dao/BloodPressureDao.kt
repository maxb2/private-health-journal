package com.privatehealthjournal.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.privatehealthjournal.data.entity.BloodPressureEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface BloodPressureDao {
    @Query("SELECT * FROM blood_pressure_entries ORDER BY timestamp DESC")
    fun getAllBloodPressureEntries(): Flow<List<BloodPressureEntry>>

    @Query("SELECT * FROM blood_pressure_entries ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentBloodPressureEntries(limit: Int): Flow<List<BloodPressureEntry>>

    @Query("SELECT * FROM blood_pressure_entries WHERE id = :id")
    suspend fun getById(id: Long): BloodPressureEntry?

    @Insert
    suspend fun insert(entry: BloodPressureEntry): Long

    @Update
    suspend fun update(entry: BloodPressureEntry)

    @Delete
    suspend fun delete(entry: BloodPressureEntry)

    @Query("DELETE FROM blood_pressure_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}
