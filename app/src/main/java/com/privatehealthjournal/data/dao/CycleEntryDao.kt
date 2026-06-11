package com.privatehealthjournal.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.privatehealthjournal.data.entity.CycleEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface CycleEntryDao {
    @Query("SELECT * FROM cycle_entries ORDER BY timestamp DESC")
    fun getAllCycleEntries(): Flow<List<CycleEntry>>

    @Query("SELECT * FROM cycle_entries ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentCycleEntries(limit: Int): Flow<List<CycleEntry>>

    @Query("SELECT * FROM cycle_entries WHERE id = :id")
    suspend fun getById(id: Long): CycleEntry?

    @Insert
    suspend fun insert(entry: CycleEntry): Long

    @Update
    suspend fun update(entry: CycleEntry)

    @Delete
    suspend fun delete(entry: CycleEntry)

    @Query("DELETE FROM cycle_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}
