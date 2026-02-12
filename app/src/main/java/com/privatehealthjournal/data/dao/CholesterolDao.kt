package com.privatehealthjournal.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.privatehealthjournal.data.entity.CholesterolEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface CholesterolDao {
    @Query("SELECT * FROM cholesterol_entries ORDER BY timestamp DESC")
    fun getAllCholesterolEntries(): Flow<List<CholesterolEntry>>

    @Query("SELECT * FROM cholesterol_entries ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentCholesterolEntries(limit: Int): Flow<List<CholesterolEntry>>

    @Query("SELECT * FROM cholesterol_entries WHERE id = :id")
    suspend fun getById(id: Long): CholesterolEntry?

    @Insert
    suspend fun insert(entry: CholesterolEntry): Long

    @Update
    suspend fun update(entry: CholesterolEntry)

    @Delete
    suspend fun delete(entry: CholesterolEntry)

    @Query("DELETE FROM cholesterol_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}
