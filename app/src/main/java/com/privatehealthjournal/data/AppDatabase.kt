package com.privatehealthjournal.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.privatehealthjournal.data.dao.BloodGlucoseDao
import com.privatehealthjournal.data.dao.BloodPressureDao
import com.privatehealthjournal.data.dao.BowelMovementDao
import com.privatehealthjournal.data.dao.CholesterolDao
import com.privatehealthjournal.data.dao.MealDao
import com.privatehealthjournal.data.dao.MedicationDao
import com.privatehealthjournal.data.dao.MedicationSetDao
import com.privatehealthjournal.data.dao.MedicationSetLogDao
import com.privatehealthjournal.data.dao.MedicationSetReminderDao
import com.privatehealthjournal.data.dao.OtherEntryDao
import com.privatehealthjournal.data.dao.SpO2Dao
import com.privatehealthjournal.data.dao.SymptomEntryDao
import com.privatehealthjournal.data.dao.WeightDao
import com.privatehealthjournal.data.entity.BloodGlucoseEntry
import com.privatehealthjournal.data.entity.BloodPressureEntry
import com.privatehealthjournal.data.entity.BowelMovementEntry
import com.privatehealthjournal.data.entity.CholesterolEntry
import com.privatehealthjournal.data.entity.FoodItem
import com.privatehealthjournal.data.entity.MealEntry
import com.privatehealthjournal.data.entity.MealTagCrossRef
import com.privatehealthjournal.data.entity.MedicationEntry
import com.privatehealthjournal.data.entity.MedicationSet
import com.privatehealthjournal.data.entity.MedicationSetItem
import com.privatehealthjournal.data.entity.MedicationSetLog
import com.privatehealthjournal.data.entity.MedicationSetReminder
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
        SpO2Entry::class,
        BloodGlucoseEntry::class,
        MedicationSet::class,
        MedicationSetItem::class,
        MedicationSetReminder::class,
        MedicationSetLog::class
    ],
    version = 12,
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
    abstract fun bloodGlucoseDao(): BloodGlucoseDao
    abstract fun medicationSetDao(): MedicationSetDao
    abstract fun medicationSetReminderDao(): MedicationSetReminderDao
    abstract fun medicationSetLogDao(): MedicationSetLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `blood_glucose_entries` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `glucoseLevel` REAL NOT NULL,
                        `unit` TEXT NOT NULL,
                        `mealContext` TEXT,
                        `notes` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `medication_sets` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL
                    )
                """.trimIndent())
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `medication_set_items` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `setId` INTEGER NOT NULL,
                        `name` TEXT NOT NULL,
                        `dosage` TEXT NOT NULL,
                        FOREIGN KEY(`setId`) REFERENCES `medication_sets`(`id`) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_medication_set_items_setId` ON `medication_set_items` (`setId`)")
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `medication_set_reminders` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `setId` INTEGER NOT NULL,
                        `hour` INTEGER NOT NULL,
                        `minute` INTEGER NOT NULL,
                        `daysOfWeek` INTEGER NOT NULL,
                        `enabled` INTEGER NOT NULL,
                        FOREIGN KEY(`setId`) REFERENCES `medication_sets`(`id`) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_medication_set_reminders_setId` ON `medication_set_reminders` (`setId`)")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `medication_set_logs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `setId` INTEGER NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        FOREIGN KEY(`setId`) REFERENCES `medication_sets`(`id`) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_medication_set_logs_setId` ON `medication_set_logs` (`setId`)")
            }
        }

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE meal_entries ADD COLUMN pointCost INTEGER")
                database.execSQL("ALTER TABLE other_entries ADD COLUMN pointCredit INTEGER")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "food_symptom_log_database"
                )
                    .addMigrations(MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
