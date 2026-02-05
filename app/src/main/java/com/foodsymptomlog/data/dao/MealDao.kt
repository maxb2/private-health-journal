package com.foodsymptomlog.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.foodsymptomlog.data.entity.FoodItem
import com.foodsymptomlog.data.entity.MealEntry
import com.foodsymptomlog.data.entity.MealTagCrossRef
import com.foodsymptomlog.data.entity.MealWithDetails
import com.foodsymptomlog.data.entity.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Transaction
    @Query("SELECT * FROM meal_entries ORDER BY timestamp DESC")
    fun getAllMealsWithDetails(): Flow<List<MealWithDetails>>

    @Transaction
    @Query("SELECT * FROM meal_entries ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentMealsWithDetails(limit: Int): Flow<List<MealWithDetails>>

    @Insert
    suspend fun insertMeal(meal: MealEntry): Long

    @Update
    suspend fun updateMeal(meal: MealEntry)

    @Transaction
    @Query("SELECT * FROM meal_entries WHERE id = :id")
    suspend fun getMealWithDetailsById(id: Long): MealWithDetails?

    @Insert
    suspend fun insertFoodItem(foodItem: FoodItem): Long

    @Insert
    suspend fun insertFoodItems(foodItems: List<FoodItem>)

    @Query("DELETE FROM food_items WHERE mealId = :mealId")
    suspend fun deleteFoodItemsByMealId(mealId: Long)

    @Query("DELETE FROM meal_tag_cross_ref WHERE mealId = :mealId")
    suspend fun deleteMealTagCrossRefsByMealId(mealId: Long)

    @Insert
    suspend fun insertTag(tag: Tag): Long

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getTagByName(name: String): Tag?

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<Tag>>

    @Insert
    suspend fun insertMealTagCrossRef(crossRef: MealTagCrossRef)

    @Delete
    suspend fun deleteMeal(meal: MealEntry)

    @Query("DELETE FROM meal_entries WHERE id = :id")
    suspend fun deleteMealById(id: Long)

    @Transaction
    suspend fun insertMealWithDetails(
        meal: MealEntry,
        foods: List<String>,
        tagNames: List<String>
    ): Long {
        val mealId = insertMeal(meal)

        // Insert food items
        val foodItems = foods.map { FoodItem(mealId = mealId, name = it) }
        insertFoodItems(foodItems)

        // Insert tags (get or create)
        for (tagName in tagNames) {
            val existingTag = getTagByName(tagName)
            val tagId = existingTag?.id ?: insertTag(Tag(name = tagName))
            insertMealTagCrossRef(MealTagCrossRef(mealId = mealId, tagId = tagId))
        }

        return mealId
    }

    @Transaction
    suspend fun updateMealWithDetails(
        meal: MealEntry,
        foods: List<String>,
        tagNames: List<String>
    ) {
        updateMeal(meal)

        // Delete existing food items and tags for this meal
        deleteFoodItemsByMealId(meal.id)
        deleteMealTagCrossRefsByMealId(meal.id)

        // Re-insert food items
        val foodItems = foods.map { FoodItem(mealId = meal.id, name = it) }
        insertFoodItems(foodItems)

        // Re-insert tags
        for (tagName in tagNames) {
            val existingTag = getTagByName(tagName)
            val tagId = existingTag?.id ?: insertTag(Tag(name = tagName))
            insertMealTagCrossRef(MealTagCrossRef(mealId = meal.id, tagId = tagId))
        }
    }
}
