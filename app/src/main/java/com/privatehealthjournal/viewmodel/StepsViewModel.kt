package com.privatehealthjournal.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.privatehealthjournal.data.entity.StepCountEntry
import com.privatehealthjournal.data.entity.StepSource
import com.privatehealthjournal.data.preferences.AppPreferences
import com.privatehealthjournal.data.preferences.BudgetPreferences
import com.privatehealthjournal.data.repository.StepsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StepsViewModel(
    private val repository: StepsRepository,
    application: Application,
) : AndroidViewModel(application) {

    val allStepCountEntries: StateFlow<List<StepCountEntry>> = repository.allStepCountEntries
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val recentStepCountEntries: StateFlow<List<StepCountEntry>> = repository.getRecentStepCountEntries(5)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val showStepCounting: StateFlow<Boolean> = AppPreferences.getShowStepCounting(application)
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val stepSensorEnabled: StateFlow<Boolean> = AppPreferences.getStepSensorEnabled(application)
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val healthConnectEnabled: StateFlow<Boolean> = AppPreferences.getHealthConnectEnabled(application)
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val stepsPerPointCredit: StateFlow<Int?> = BudgetPreferences.getStepsPerPointCredit(application)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _editingStepCount = MutableStateFlow<StepCountEntry?>(null)
    val editingStepCount: StateFlow<StepCountEntry?> = _editingStepCount.asStateFlow()

    fun addStepCount(dateEpochDay: Long, steps: Int, notes: String = "") {
        viewModelScope.launch {
            repository.recordStepCount(dateEpochDay, steps, StepSource.MANUAL, notes)
        }
    }

    fun updateStepCount(entry: StepCountEntry) {
        viewModelScope.launch { repository.updateStepCount(entry) }
    }

    fun deleteStepCount(entry: StepCountEntry) {
        viewModelScope.launch { repository.deleteStepCount(entry) }
    }

    fun loadStepCountForEditing(id: Long) {
        viewModelScope.launch { _editingStepCount.value = repository.getStepCountById(id) }
    }

    fun clearEditingStepCount() { _editingStepCount.value = null }

    fun setShowStepCounting(show: Boolean) {
        viewModelScope.launch { AppPreferences.setShowStepCounting(getApplication(), show) }
    }

    fun setStepSensorEnabled(enabled: Boolean) {
        viewModelScope.launch { AppPreferences.setStepSensorEnabled(getApplication(), enabled) }
    }

    fun setHealthConnectEnabled(enabled: Boolean) {
        viewModelScope.launch { AppPreferences.setHealthConnectEnabled(getApplication(), enabled) }
    }

    fun saveStepsPerPointCredit(value: Int?) {
        viewModelScope.launch {
            if (value == null || value <= 0) {
                BudgetPreferences.clearStepsPerPointCredit(getApplication())
            } else {
                BudgetPreferences.setStepsPerPointCredit(getApplication(), value)
            }
        }
    }
}
