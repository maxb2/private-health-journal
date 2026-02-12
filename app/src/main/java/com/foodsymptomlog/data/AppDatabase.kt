package com.foodsymptomlog.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.foodsymptomlog.data.dao.BloodPressureDao
import com.foodsymptomlog.data.dao.BowelMovementDao
import com.foodsymptomlog.data.dao.CholesterolDao
import com.foodsymptomlog.data.dao.MealDao
import com.foodsymptomlog.data.dao.MedicationDao
import com.foodsymptomlog.data.dao.OtherEntryDao
import com.foodsymptomlog.data.dao.SymptomEntryDao
import com.foodsymptomlog.data.dao.WeightDao
import com.foodsymptomlog.data.entity.BloodPressureEntry
import com.foodsymptomlog.data.entity.BowelMovementEntry
import com.foodsymptomlog.data.entity.CholesterolEntry
import com.foodsymptomlog.data.entity.FoodItem
import com.foodsymptomlog.data.entity.MealEntry
import com.foodsymptomlog.data.entity.MealTagCrossRef
import com.foodsymptomlog.data.entity.MedicationEntry
import com.foodsymptomlog.data.entity.OtherEntry
import com.foodsymptomlog.data.entity.SymptomEntry
import com.foodsymptomlog.data.entity.Tag
import com.foodsymptomlog.data.entity.WeightEntry

@Database(
    entities = [
        MealEntry::class,
        FoodItem::class,
        Tag::class,
        MealTagCrossRef::class,
        SymptomEntry::class,
        BowelMovementEntry::class,
        MedicationEntry::class,
        OtherEntry::class,
        BloodPressureEntry::class,
        CholesterolEntry::class,
        WeightEntry::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun symptomEntryDao(): SymptomEntryDao
    abstract fun bowelMovementDao(): BowelMovementDao
    abstract fun medicationDao(): MedicationDao
    abstract fun otherEntryDao(): OtherEntryDao
    abstract fun bloodPressureDao(): BloodPressureDao
    abstract fun cholesterolDao(): CholesterolDao
    abstract fun weightDao(): WeightDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "food_symptom_log_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
