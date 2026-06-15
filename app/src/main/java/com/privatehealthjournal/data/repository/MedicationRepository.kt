package com.privatehealthjournal.data.repository

import com.privatehealthjournal.data.dao.MedicationDao
import com.privatehealthjournal.data.dao.MedicationSetDao
import com.privatehealthjournal.data.dao.MedicationSetLogDao
import com.privatehealthjournal.data.dao.MedicationSetReminderDao
import com.privatehealthjournal.data.entity.MedicationEntry
import com.privatehealthjournal.data.entity.MedicationSet
import com.privatehealthjournal.data.entity.MedicationSetLog
import com.privatehealthjournal.data.entity.MedicationSetReminder
import com.privatehealthjournal.data.entity.MedicationSetWithItems
import com.privatehealthjournal.di.TransactionRunner
import kotlinx.coroutines.flow.Flow

class MedicationRepository(
    private val medicationDao: MedicationDao,
    private val medicationSetDao: MedicationSetDao,
    private val medicationSetReminderDao: MedicationSetReminderDao,
    private val medicationSetLogDao: MedicationSetLogDao,
    private val tx: TransactionRunner,
) {
    val allMedications: Flow<List<MedicationEntry>> = medicationDao.getAllMedications()
    val allMedicationNames: Flow<List<String>> = medicationDao.getAllMedicationNames()
    val allMedicationSets: Flow<List<MedicationSetWithItems>> = medicationSetDao.getAllSetsWithItems()

    fun getRecentMedications(limit: Int = 5): Flow<List<MedicationEntry>> =
        medicationDao.getRecentMedications(limit)

    suspend fun insertMedication(entry: MedicationEntry): Long = medicationDao.insert(entry)
    suspend fun updateMedication(entry: MedicationEntry) = medicationDao.update(entry)
    suspend fun deleteMedication(entry: MedicationEntry) = medicationDao.delete(entry)
    suspend fun getMedicationById(id: Long): MedicationEntry? = medicationDao.getById(id)

    // Set CRUD
    suspend fun insertMedicationSet(name: String, items: List<Pair<String, String>>): Long =
        medicationSetDao.insertSetWithItems(MedicationSet(name = name), items)
    suspend fun updateMedicationSet(set: MedicationSet, items: List<Pair<String, String>>) =
        medicationSetDao.updateSetWithItems(set, items)
    suspend fun deleteMedicationSetById(id: Long) = medicationSetDao.deleteSetById(id)
    suspend fun getMedicationSetWithItemsById(id: Long): MedicationSetWithItems? =
        medicationSetDao.getSetWithItemsById(id)

    // Reminders
    fun getRemindersForSet(setId: Long): Flow<List<MedicationSetReminder>> =
        medicationSetReminderDao.getRemindersForSet(setId)
    fun getAllReminders(): Flow<List<MedicationSetReminder>> = medicationSetReminderDao.getAllReminders()
    suspend fun getAllEnabledReminders(): List<MedicationSetReminder> =
        medicationSetReminderDao.getAllEnabledReminders()
    suspend fun getReminderById(id: Long): MedicationSetReminder? = medicationSetReminderDao.getById(id)
    suspend fun insertReminder(reminder: MedicationSetReminder): Long = medicationSetReminderDao.insert(reminder)
    suspend fun updateReminder(reminder: MedicationSetReminder) = medicationSetReminderDao.update(reminder)
    suspend fun deleteReminder(reminder: MedicationSetReminder) = medicationSetReminderDao.delete(reminder)
    suspend fun deleteReminderById(id: Long) = medicationSetReminderDao.deleteById(id)

    // Logs
    suspend fun insertMedicationSetLog(log: MedicationSetLog): Long = medicationSetLogDao.insert(log)
    fun getAllMedicationSetLogs(): Flow<List<MedicationSetLog>> = medicationSetLogDao.getAllLogs()

    /**
     * Atomic: one MedicationEntry per item plus one MedicationSetLog. Either everything
     * commits or nothing does, so a crash mid-loop can't leave orphan MedicationEntries
     * with no log row (which would let the reminder fire again the same day).
     */
    suspend fun logMedicationSetAtomically(
        setId: Long,
        items: List<MedicationSetItemSpec>,
        timestamp: Long,
        notes: String,
    ) {
        tx {
            items.forEach { item ->
                medicationDao.insert(
                    MedicationEntry(
                        name = item.name,
                        dosage = item.dosage,
                        notes = notes,
                        timestamp = timestamp,
                    )
                )
            }
            medicationSetLogDao.insert(MedicationSetLog(setId = setId, timestamp = timestamp))
        }
    }

    data class MedicationSetItemSpec(val name: String, val dosage: String)

    suspend fun hasSetBeenLoggedToday(setId: Long): Boolean {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        val endOfDay = startOfDay + 86_400_000L
        return medicationSetLogDao.getLogForSetOnDay(setId, startOfDay, endOfDay) != null
    }
}
