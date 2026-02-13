package com.privatehealthjournal.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.privatehealthjournal.data.entity.MedicationSet
import com.privatehealthjournal.data.entity.MedicationSetItem
import com.privatehealthjournal.data.entity.MedicationSetWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationSetDao {
    @Transaction
    @Query("SELECT * FROM medication_sets ORDER BY name ASC")
    fun getAllSetsWithItems(): Flow<List<MedicationSetWithItems>>

    @Transaction
    @Query("SELECT * FROM medication_sets WHERE id = :id")
    suspend fun getSetWithItemsById(id: Long): MedicationSetWithItems?

    @Insert
    suspend fun insertSet(set: MedicationSet): Long

    @Update
    suspend fun updateSet(set: MedicationSet)

    @Delete
    suspend fun deleteSet(set: MedicationSet)

    @Query("DELETE FROM medication_sets WHERE id = :id")
    suspend fun deleteSetById(id: Long)

    @Insert
    suspend fun insertItems(items: List<MedicationSetItem>)

    @Query("DELETE FROM medication_set_items WHERE setId = :setId")
    suspend fun deleteItemsBySetId(setId: Long)

    @Transaction
    suspend fun insertSetWithItems(
        set: MedicationSet,
        items: List<Pair<String, String>>
    ): Long {
        val setId = insertSet(set)
        val setItems = items.map { (name, dosage) ->
            MedicationSetItem(setId = setId, name = name, dosage = dosage)
        }
        insertItems(setItems)
        return setId
    }

    @Transaction
    suspend fun updateSetWithItems(
        set: MedicationSet,
        items: List<Pair<String, String>>
    ) {
        updateSet(set)
        deleteItemsBySetId(set.id)
        val setItems = items.map { (name, dosage) ->
            MedicationSetItem(setId = set.id, name = name, dosage = dosage)
        }
        insertItems(setItems)
    }
}
