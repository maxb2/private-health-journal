package com.privatehealthjournal.data.repository

import com.privatehealthjournal.data.dao.StepCountDao
import com.privatehealthjournal.data.entity.StepCountEntry
import com.privatehealthjournal.data.entity.StepSource
import kotlinx.coroutines.flow.Flow

class StepsRepository(
    private val stepCountDao: StepCountDao,
) {
    val allStepCountEntries: Flow<List<StepCountEntry>> = stepCountDao.getAllStepCountEntries()

    fun getRecentStepCountEntries(limit: Int = 5): Flow<List<StepCountEntry>> =
        stepCountDao.getRecentStepCountEntries(limit)

    suspend fun insertStepCount(entry: StepCountEntry): Long = stepCountDao.insert(entry)
    suspend fun updateStepCount(entry: StepCountEntry) = stepCountDao.update(entry)
    suspend fun deleteStepCount(entry: StepCountEntry) = stepCountDao.delete(entry)
    suspend fun deleteStepCountById(id: Long) = stepCountDao.deleteById(id)
    suspend fun getStepCountById(id: Long): StepCountEntry? = stepCountDao.getById(id)
    suspend fun getStepCountByEpochDay(day: Long): StepCountEntry? = stepCountDao.getByEpochDay(day)
    suspend fun upsertStepCount(entry: StepCountEntry): Long = stepCountDao.upsert(entry)

    /**
     * Merge rule: a higher-priority source never gets overwritten by a lower-priority one.
     * Priority: MANUAL > HEALTH_CONNECT > SENSOR. A same-or-higher-priority write replaces.
     * Returns the row id (existing or new).
     */
    suspend fun recordStepCount(
        dateEpochDay: Long,
        steps: Int,
        source: StepSource,
        notes: String = "",
        timestamp: Long = System.currentTimeMillis(),
    ): Long {
        val existing = stepCountDao.getByEpochDay(dateEpochDay)
        if (existing != null && stepSourcePriority(existing.source) > stepSourcePriority(source)) {
            return existing.id
        }
        val entry = StepCountEntry(
            id = existing?.id ?: 0,
            dateEpochDay = dateEpochDay,
            steps = steps,
            source = source,
            notes = notes,
            timestamp = timestamp,
        )
        return stepCountDao.upsert(entry)
    }

    private fun stepSourcePriority(source: StepSource): Int = when (source) {
        StepSource.MANUAL -> 2
        StepSource.HEALTH_CONNECT -> 1
        StepSource.SENSOR -> 0
    }
}
