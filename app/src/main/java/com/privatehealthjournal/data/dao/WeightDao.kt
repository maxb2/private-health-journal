package com.privatehealthjournal.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.privatehealthjournal.data.entity.WeightEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_entries ORDER BY timestamp DESC")
    fun getAllWeightEntries(): Flow<List<WeightEntry>>

    @Query("SELECT * FROM weight_entries ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentWeightEntries(limit: Int): Flow<List<WeightEntry>>

    @Query("SELECT * FROM weight_entries WHERE id = :id")
    suspend fun getById(id: Long): WeightEntry?

    @Insert
    suspend fun insert(entry: WeightEntry): Long

    @Update
    suspend fun update(entry: WeightEntry)

    @Delete
    suspend fun delete(entry: WeightEntry)

    @Query("DELETE FROM weight_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}
