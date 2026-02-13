package com.privatehealthjournal.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.privatehealthjournal.data.entity.MedicationSetLog

@Dao
interface MedicationSetLogDao {
    @Insert
    suspend fun insert(log: MedicationSetLog): Long

    @Query("SELECT * FROM medication_set_logs WHERE setId = :setId AND timestamp >= :startOfDay AND timestamp < :endOfDay LIMIT 1")
    suspend fun getLogForSetOnDay(setId: Long, startOfDay: Long, endOfDay: Long): MedicationSetLog?
}
