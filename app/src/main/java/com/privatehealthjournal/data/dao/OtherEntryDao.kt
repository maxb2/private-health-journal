package com.privatehealthjournal.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.privatehealthjournal.data.entity.OtherEntry
import com.privatehealthjournal.data.entity.OtherEntryType
import kotlinx.coroutines.flow.Flow

@Dao
interface OtherEntryDao {
    @Query("SELECT * FROM other_entries ORDER BY timestamp DESC")
    fun getAllOtherEntries(): Flow<List<OtherEntry>>

    @Query("SELECT * FROM other_entries WHERE entryType = :type ORDER BY timestamp DESC")
    fun getOtherEntriesByType(type: OtherEntryType): Flow<List<OtherEntry>>

    @Query("SELECT * FROM other_entries ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentOtherEntries(limit: Int): Flow<List<OtherEntry>>

    @Query("SELECT * FROM other_entries WHERE id = :id")
    suspend fun getById(id: Long): OtherEntry?

    @Insert
    suspend fun insert(entry: OtherEntry): Long

    @Update
    suspend fun update(entry: OtherEntry)

    @Delete
    suspend fun delete(entry: OtherEntry)

    @Query("DELETE FROM other_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}
