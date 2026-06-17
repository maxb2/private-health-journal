package com.privatehealthjournal.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.privatehealthjournal.data.AppDatabase
import com.privatehealthjournal.data.entity.MedicationSet
import com.privatehealthjournal.di.TransactionRunner
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MedicationRepositoryAtomicityTest {

    private lateinit var db: AppDatabase
    private lateinit var repo: MedicationRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        val tx = TransactionRunner(db)
        repo = MedicationRepository(
            db.medicationDao(),
            db.medicationSetDao(),
            db.medicationSetReminderDao(),
            db.medicationSetLogDao(),
            tx,
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
                MedicationRepository.MedicationSetItemSpec("Atorvastatin", "20mg"),
                MedicationRepository.MedicationSetItemSpec("Metformin", "500mg")
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
                items = listOf(MedicationRepository.MedicationSetItemSpec("Atorvastatin", "20mg")),
                timestamp = 1L,
                notes = "should not commit"
            )
            false
        } catch (e: Exception) {
            true
        }

        assertTrue("FK violation should have surfaced", threw)
        assertEquals(0, repo.allMedications.first().size)
        assertEquals(0, repo.getAllMedicationSetLogs().first().size)
    }
}
