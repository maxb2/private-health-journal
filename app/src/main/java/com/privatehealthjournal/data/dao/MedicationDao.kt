package com.privatehealthjournal.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.privatehealthjournal.data.entity.MedicationEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medication_entries ORDER BY timestamp DESC")
    fun getAllMedications(): Flow<List<MedicationEntry>>

    @Query("SELECT * FROM medication_entries ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentMedications(limit: Int): Flow<List<MedicationEntry>>

    @Query("SELECT DISTINCT name FROM medication_entries ORDER BY name ASC")
    fun getAllMedicationNames(): Flow<List<String>>

    @Query("SELECT * FROM medication_entries WHERE id = :id")
    suspend fun getById(id: Long): MedicationEntry?

    @Insert
    suspend fun insert(entry: MedicationEntry): Long

    @Update
    suspend fun update(entry: MedicationEntry)

    @Delete
    suspend fun delete(entry: MedicationEntry)

    @Query("DELETE FROM medication_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}
