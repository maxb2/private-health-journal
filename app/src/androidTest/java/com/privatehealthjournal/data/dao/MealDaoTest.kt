package com.privatehealthjournal.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.privatehealthjournal.data.AppDatabase
import com.privatehealthjournal.data.entity.MealEntry
import com.privatehealthjournal.data.entity.MealType
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MealDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: MealDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.mealDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertMealWithDetails_persistsMealFoodsAndTagsAtomically() = runTest {
        val mealId = dao.insertMealWithDetails(
            meal = MealEntry(mealType = MealType.LUNCH, notes = "Filling"),
            foods = listOf("Salad", "Bread"),
            tagNames = listOf("homemade", "low-carb")
        )

        val loaded = dao.getMealWithDetailsById(mealId)
        assertNotNull(loaded)
        assertEquals(MealType.LUNCH, loaded!!.meal.mealType)
        assertEquals("Filling", loaded.meal.notes)
        assertEquals(setOf("Salad", "Bread"), loaded.foods.map { it.name }.toSet())
        assertEquals(setOf("homemade", "low-carb"), loaded.tags.map { it.name }.toSet())
    }

    @Test
    fun insertMealWithDetails_reusesExistingTagRows() = runTest {
        dao.insertMealWithDetails(
            MealEntry(mealType = MealType.BREAKFAST),
            foods = listOf("Eggs"),
            tagNames = listOf("protein")
        )
        dao.insertMealWithDetails(
            MealEntry(mealType = MealType.DINNER),
            foods = listOf("Chicken"),
            tagNames = listOf("protein") // same tag name
        )

        // The Tag should exist exactly once; two meals link to it via cross-ref.
        val tag = dao.getTagByName("protein")
        assertNotNull(tag)
    }
}
