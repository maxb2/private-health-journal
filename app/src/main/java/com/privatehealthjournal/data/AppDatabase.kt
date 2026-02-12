package com.privatehealthjournal.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.privatehealthjournal.data.dao.BloodPressureDao
import com.privatehealthjournal.data.dao.BowelMovementDao
import com.privatehealthjournal.data.dao.CholesterolDao
import com.privatehealthjournal.data.dao.MealDao
import com.privatehealthjournal.data.dao.MedicationDao
import com.privatehealthjournal.data.dao.OtherEntryDao
import com.privatehealthjournal.data.dao.SpO2Dao
import com.privatehealthjournal.data.dao.SymptomEntryDao
import com.privatehealthjournal.data.dao.WeightDao
import com.privatehealthjournal.data.entity.BloodPressureEntry
import com.privatehealthjournal.data.entity.BowelMovementEntry
import com.privatehealthjournal.data.entity.CholesterolEntry
import com.privatehealthjournal.data.entity.FoodItem
import com.privatehealthjournal.data.entity.MealEntry
import com.privatehealthjournal.data.entity.MealTagCrossRef
import com.privatehealthjournal.data.entity.MedicationEntry
import com.privatehealthjournal.data.entity.OtherEntry
import com.privatehealthjournal.data.entity.SpO2Entry
import com.privatehealthjournal.data.entity.SymptomEntry
import com.privatehealthjournal.data.entity.Tag
import com.privatehealthjournal.data.entity.WeightEntry

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
        WeightEntry::class,
        SpO2Entry::class
    ],
    version = 8,
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
    abstract fun spO2Dao(): SpO2Dao

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
