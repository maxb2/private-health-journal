package com.privatehealthjournal.data.repository

import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.privatehealthjournal.data.AppDatabase
import com.privatehealthjournal.data.entity.MedicationSet
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LogRepositoryAtomicityTest {

    private lateinit var db: AppDatabase
    private lateinit var repo: LogRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        repo = LogRepository(
            db.mealDao(), db.symptomEntryDao(), db.bowelMovementDao(),
            db.medicationDao(), db.otherEntryDao(), db.bloodPressureDao(),
            db.cholesterolDao(), db.weightDao(), db.spO2Dao(),
            db.bloodGlucoseDao(), db.medicationSetDao(),
            db.medicationSetReminderDao(), db.medicationSetLogDao(),
            db.cycleEntryDao(), db.stepCountDao(),
            transaction = { block -> db.withTransaction { block() } }
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun logMedicationSetAtomically_commitsAllRows() = runTest {
        val setId = db.medicationSetDao().insertSet(MedicationSet(name = "Evening"))
        val timestamp = 1_700_000_000_000L

        repo.logMedicationSetAtomically(
            setId = setId,
            items = listOf(
                LogRepository.MedicationSetItemSpec("Atorvastatin", "20mg"),
                LogRepository.MedicationSetItemSpec("Metformin", "500mg")
            ),
            timestamp = timestamp,
            notes = "Logged from set: Evening"
        )

        val medications = repo.allMedications.first()
        assertEquals(2, medications.size)
        assertTrue(medications.all { it.timestamp == timestamp })
        assertTrue(medications.all { it.notes == "Logged from set: Evening" })

        val logs = repo.getAllMedicationSetLogs().first()
        assertEquals(1, logs.size)
        assertEquals(setId, logs[0].setId)
        assertEquals(timestamp, logs[0].timestamp)
    }

    @Test
    fun logMedicationSetAtomically_rollsBackOnFailure() = runTest {
        // setId does not exist → MedicationSetLog insert violates the FK and rolls back the txn.
        val invalidSetId = 99_999L

        val threw = try {
            repo.logMedicationSetAtomically(
                setId = invalidSetId,
                items = listOf(LogRepository.MedicationSetItemSpec("Atorvastatin", "20mg")),
                timestamp = 1L,
                notes = "should not commit"
            )
            false
        } catch (e: Exception) {
            true
        }

        assertTrue("FK violation should have surfaced", threw)
        // Neither the MedicationEntry rows nor the MedicationSetLog row should have committed.
        assertEquals(0, repo.allMedications.first().size)
        assertEquals(0, repo.getAllMedicationSetLogs().first().size)
    }
}
