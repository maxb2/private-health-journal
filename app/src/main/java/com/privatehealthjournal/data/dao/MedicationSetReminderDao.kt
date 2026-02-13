package com.privatehealthjournal.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.privatehealthjournal.data.entity.MedicationSetReminder
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationSetReminderDao {
    @Query("SELECT * FROM medication_set_reminders WHERE setId = :setId ORDER BY hour, minute")
    fun getRemindersForSet(setId: Long): Flow<List<MedicationSetReminder>>

    @Query("SELECT * FROM medication_set_reminders ORDER BY hour, minute")
    fun getAllReminders(): Flow<List<MedicationSetReminder>>

    @Query("SELECT * FROM medication_set_reminders WHERE enabled = 1")
    suspend fun getAllEnabledReminders(): List<MedicationSetReminder>

    @Query("SELECT * FROM medication_set_reminders WHERE id = :id")
    suspend fun getById(id: Long): MedicationSetReminder?

    @Insert
    suspend fun insert(reminder: MedicationSetReminder): Long

    @Update
    suspend fun update(reminder: MedicationSetReminder)

    @Delete
    suspend fun delete(reminder: MedicationSetReminder)

    @Query("DELETE FROM medication_set_reminders WHERE id = :id")
    suspend fun deleteById(id: Long)
}
