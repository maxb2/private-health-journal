package com.privatehealthjournal.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.privatehealthjournal.data.entity.StepCountEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface StepCountDao {
    @Query("SELECT * FROM step_count_entries ORDER BY dateEpochDay DESC")
    fun getAllStepCountEntries(): Flow<List<StepCountEntry>>

    @Query("SELECT * FROM step_count_entries ORDER BY dateEpochDay DESC LIMIT :limit")
    fun getRecentStepCountEntries(limit: Int): Flow<List<StepCountEntry>>

    @Query("SELECT * FROM step_count_entries WHERE id = :id")
    suspend fun getById(id: Long): StepCountEntry?

    @Query("SELECT * FROM step_count_entries WHERE dateEpochDay = :day LIMIT 1")
    suspend fun getByEpochDay(day: Long): StepCountEntry?

    @Insert
    suspend fun insert(entry: StepCountEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: StepCountEntry): Long

    @Update
    suspend fun update(entry: StepCountEntry)

    @Delete
    suspend fun delete(entry: StepCountEntry)

    @Query("DELETE FROM step_count_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}
