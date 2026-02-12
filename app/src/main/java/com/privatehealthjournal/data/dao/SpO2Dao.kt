package com.privatehealthjournal.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.privatehealthjournal.data.entity.SpO2Entry
import kotlinx.coroutines.flow.Flow

@Dao
interface SpO2Dao {
    @Query("SELECT * FROM spo2_entries ORDER BY timestamp DESC")
    fun getAllSpO2Entries(): Flow<List<SpO2Entry>>

    @Query("SELECT * FROM spo2_entries ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSpO2Entries(limit: Int): Flow<List<SpO2Entry>>

    @Query("SELECT * FROM spo2_entries WHERE id = :id")
    suspend fun getById(id: Long): SpO2Entry?

    @Insert
    suspend fun insert(entry: SpO2Entry): Long

    @Update
    suspend fun update(entry: SpO2Entry)

    @Delete
    suspend fun delete(entry: SpO2Entry)

    @Query("DELETE FROM spo2_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}
