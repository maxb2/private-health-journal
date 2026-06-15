package com.privatehealthjournal.di

import android.app.Application
import androidx.room.withTransaction
import com.privatehealthjournal.data.AppDatabase
import com.privatehealthjournal.data.repository.BiometricsRepository
import com.privatehealthjournal.data.repository.JournalRepository
import com.privatehealthjournal.data.repository.MedicationRepository
import com.privatehealthjournal.data.repository.StepsRepository

open class TransactionRunner(private val database: AppDatabase) {
    open suspend operator fun <T> invoke(block: suspend () -> T): T =
        database.withTransaction { block() }
}

class AppContainer(private val app: Application) {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(app) }

    val transactionRunner: TransactionRunner by lazy { TransactionRunner(database) }

    val stepsRepository: StepsRepository by lazy {
        StepsRepository(database.stepCountDao())
    }

    val journalRepository: JournalRepository by lazy {
        JournalRepository(
            database.mealDao(),
            database.symptomEntryDao(),
            database.bowelMovementDao(),
            database.otherEntryDao(),
            database.cycleEntryDao(),
        )
    }

    val medicationRepository: MedicationRepository by lazy {
        MedicationRepository(
            database.medicationDao(),
            database.medicationSetDao(),
            database.medicationSetReminderDao(),
            database.medicationSetLogDao(),
            transactionRunner,
        )
    }

    val biometricsRepository: BiometricsRepository by lazy {
        BiometricsRepository(
            database.bloodPressureDao(),
            database.cholesterolDao(),
            database.weightDao(),
            database.spO2Dao(),
            database.bloodGlucoseDao(),
        )
    }
}
