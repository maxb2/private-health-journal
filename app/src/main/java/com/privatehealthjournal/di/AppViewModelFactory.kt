package com.privatehealthjournal.di

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.privatehealthjournal.viewmodel.BiometricsViewModel
import com.privatehealthjournal.viewmodel.JournalViewModel
import com.privatehealthjournal.viewmodel.MedicationViewModel
import com.privatehealthjournal.viewmodel.SettingsViewModel
import com.privatehealthjournal.viewmodel.StepsViewModel

class AppViewModelFactory(
    private val container: AppContainer,
    private val application: Application,
) : ViewModelProvider.AndroidViewModelFactory(application) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when (modelClass) {
        StepsViewModel::class.java -> StepsViewModel(container.stepsRepository, application) as T
        BiometricsViewModel::class.java -> BiometricsViewModel(container.biometricsRepository, application) as T
        MedicationViewModel::class.java -> MedicationViewModel(container.medicationRepository, application) as T
        JournalViewModel::class.java -> JournalViewModel(container.journalRepository, application) as T
        SettingsViewModel::class.java -> SettingsViewModel(
            container.journalRepository,
            container.medicationRepository,
            container.biometricsRepository,
            container.stepsRepository,
            container.transactionRunner,
            application,
        ) as T
        else -> super.create(modelClass)
    }
}
