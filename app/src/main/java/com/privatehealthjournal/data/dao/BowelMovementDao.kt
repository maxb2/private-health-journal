package com.privatehealthjournal.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.privatehealthjournal.data.entity.BowelMovementEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface BowelMovementDao {
    @Query("SELECT * FROM bowel_movement_entries ORDER BY timestamp DESC")
    fun getAllBowelMovements(): Flow<List<BowelMovementEntry>>

    @Query("SELECT * FROM bowel_movement_entries ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentBowelMovements(limit: Int): Flow<List<BowelMovementEntry>>

    @Insert
    suspend fun insert(entry: BowelMovementEntry): Long

    @Update
    suspend fun update(entry: BowelMovementEntry)

    @Query("SELECT * FROM bowel_movement_entries WHERE id = :id")
    suspend fun getById(id: Long): BowelMovementEntry?

    @Delete
    suspend fun delete(entry: BowelMovementEntry)

    @Query("DELETE FROM bowel_movement_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}
