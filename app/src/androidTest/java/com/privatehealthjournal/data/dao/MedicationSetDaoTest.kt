package com.privatehealthjournal.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.privatehealthjournal.data.AppDatabase
import com.privatehealthjournal.data.entity.MedicationSet
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MedicationSetDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: MedicationSetDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.medicationSetDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertSetWithItems_persistsSetAndItemsAtomically() = runTest {
        val items = listOf("Metformin" to "500mg", "Atorvastatin" to "20mg")

        val setId = dao.insertSetWithItems(MedicationSet(name = "Morning"), items)

        val loaded = dao.getSetWithItemsById(setId)
        assertNotNull(loaded)
        assertEquals("Morning", loaded!!.set.name)
        assertEquals(2, loaded.items.size)
        assertEquals(setOf("Metformin", "Atorvastatin"), loaded.items.map { it.name }.toSet())
        assertEquals(setOf("500mg", "20mg"), loaded.items.map { it.dosage }.toSet())
    }

    @Test
    fun updateSetWithItems_replacesItems() = runTest {
        val setId = dao.insertSetWithItems(
            MedicationSet(name = "Morning"),
            listOf("Old" to "1mg")
        )

        dao.updateSetWithItems(
            MedicationSet(id = setId, name = "Morning Renamed"),
            listOf("New A" to "10mg", "New B" to "20mg")
        )

        val loaded = dao.getSetWithItemsById(setId)
        assertNotNull(loaded)
        assertEquals("Morning Renamed", loaded!!.set.name)
        assertEquals(2, loaded.items.size)
        assertEquals(setOf("New A", "New B"), loaded.items.map { it.name }.toSet())
    }

    @Test
    fun deleteSet_cascadesToItems() = runTest {
        val setId = dao.insertSetWithItems(
            MedicationSet(name = "Doomed"),
            listOf("A" to "1mg", "B" to "2mg")
        )
        val set = dao.getSetWithItemsById(setId)!!.set

        dao.deleteSet(set)

        assertNull(dao.getSetWithItemsById(setId))
        // All cross-rows for the set should be gone via ON DELETE CASCADE.
        val remaining = dao.getAllSetsWithItems().first()
        assertEquals(0, remaining.size)
    }
}
