package com.privatehealthjournal.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.privatehealthjournal.data.entity.BloodGlucoseEntry
import com.privatehealthjournal.data.entity.BloodPressureEntry
import com.privatehealthjournal.data.entity.CholesterolEntry
import com.privatehealthjournal.data.entity.GlucoseMealContext
import com.privatehealthjournal.data.entity.GlucoseUnit
import com.privatehealthjournal.data.entity.SpO2Entry
import com.privatehealthjournal.data.entity.WeightEntry
import com.privatehealthjournal.data.entity.WeightUnit
import com.privatehealthjournal.data.repository.BiometricsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BiometricsViewModel(
    private val repository: BiometricsRepository,
    application: Application,
) : AndroidViewModel(application) {

    val allBloodPressureEntries: StateFlow<List<BloodPressureEntry>> = repository.allBloodPressureEntries
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allCholesterolEntries: StateFlow<List<CholesterolEntry>> = repository.allCholesterolEntries
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allWeightEntries: StateFlow<List<WeightEntry>> = repository.allWeightEntries
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allSpO2Entries: StateFlow<List<SpO2Entry>> = repository.allSpO2Entries
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allBloodGlucoseEntries: StateFlow<List<BloodGlucoseEntry>> = repository.allBloodGlucoseEntries
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val recentBloodPressureEntries: StateFlow<List<BloodPressureEntry>> = repository.getRecentBloodPressureEntries(5)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val recentCholesterolEntries: StateFlow<List<CholesterolEntry>> = repository.getRecentCholesterolEntries(5)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val recentWeightEntries: StateFlow<List<WeightEntry>> = repository.getRecentWeightEntries(5)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val recentSpO2Entries: StateFlow<List<SpO2Entry>> = repository.getRecentSpO2Entries(5)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val recentBloodGlucoseEntries: StateFlow<List<BloodGlucoseEntry>> = repository.getRecentBloodGlucoseEntries(5)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _editingBloodPressure = MutableStateFlow<BloodPressureEntry?>(null)
    val editingBloodPressure: StateFlow<BloodPressureEntry?> = _editingBloodPressure.asStateFlow()
    private val _editingCholesterol = MutableStateFlow<CholesterolEntry?>(null)
    val editingCholesterol: StateFlow<CholesterolEntry?> = _editingCholesterol.asStateFlow()
    private val _editingWeight = MutableStateFlow<WeightEntry?>(null)
    val editingWeight: StateFlow<WeightEntry?> = _editingWeight.asStateFlow()
    private val _editingSpO2 = MutableStateFlow<SpO2Entry?>(null)
    val editingSpO2: StateFlow<SpO2Entry?> = _editingSpO2.asStateFlow()
    private val _editingBloodGlucose = MutableStateFlow<BloodGlucoseEntry?>(null)
    val editingBloodGlucose: StateFlow<BloodGlucoseEntry?> = _editingBloodGlucose.asStateFlow()

    fun addBloodPressure(
        systolic: Int, diastolic: Int, pulse: Int? = null,
        notes: String = "", timestamp: Long = System.currentTimeMillis(),
    ) {
        viewModelScope.launch {
            repository.insertBloodPressure(BloodPressureEntry(systolic = systolic, diastolic = diastolic, pulse = pulse, notes = notes, timestamp = timestamp))
        }
    }
    fun updateBloodPressure(entry: BloodPressureEntry) {
        viewModelScope.launch { repository.updateBloodPressure(entry) }
    }
    fun deleteBloodPressure(entry: BloodPressureEntry) {
        viewModelScope.launch { repository.deleteBloodPressure(entry) }
    }
    fun loadBloodPressureForEditing(id: Long) {
        viewModelScope.launch { _editingBloodPressure.value = repository.getBloodPressureById(id) }
    }

    fun addCholesterol(
        total: Int? = null, ldl: Int? = null, hdl: Int? = null, triglycerides: Int? = null,
        notes: String = "", timestamp: Long = System.currentTimeMillis(),
    ) {
        viewModelScope.launch {
            repository.insertCholesterol(CholesterolEntry(total = total, ldl = ldl, hdl = hdl, triglycerides = triglycerides, notes = notes, timestamp = timestamp))
        }
    }
    fun updateCholesterol(entry: CholesterolEntry) {
        viewModelScope.launch { repository.updateCholesterol(entry) }
    }
    fun deleteCholesterol(entry: CholesterolEntry) {
        viewModelScope.launch { repository.deleteCholesterol(entry) }
    }
    fun loadCholesterolForEditing(id: Long) {
        viewModelScope.launch { _editingCholesterol.value = repository.getCholesterolById(id) }
    }

    fun addWeight(
        weight: Double, unit: WeightUnit = WeightUnit.LB,
        notes: String = "", timestamp: Long = System.currentTimeMillis(),
    ) {
        viewModelScope.launch {
            repository.insertWeight(WeightEntry(weight = weight, unit = unit, notes = notes, timestamp = timestamp))
        }
    }
    fun updateWeight(entry: WeightEntry) {
        viewModelScope.launch { repository.updateWeight(entry) }
    }
    fun deleteWeight(entry: WeightEntry) {
        viewModelScope.launch { repository.deleteWeight(entry) }
    }
    fun loadWeightForEditing(id: Long) {
        viewModelScope.launch { _editingWeight.value = repository.getWeightById(id) }
    }

    fun addSpO2(
        spo2: Int, pulse: Int? = null,
        notes: String = "", timestamp: Long = System.currentTimeMillis(),
    ) {
        viewModelScope.launch {
            repository.insertSpO2(SpO2Entry(spo2 = spo2, pulse = pulse, notes = notes, timestamp = timestamp))
        }
    }
    fun updateSpO2(entry: SpO2Entry) {
        viewModelScope.launch { repository.updateSpO2(entry) }
    }
    fun deleteSpO2(entry: SpO2Entry) {
        viewModelScope.launch { repository.deleteSpO2(entry) }
    }
    fun loadSpO2ForEditing(id: Long) {
        viewModelScope.launch { _editingSpO2.value = repository.getSpO2ById(id) }
    }

    fun addBloodGlucose(
        glucoseLevel: Double, unit: GlucoseUnit = GlucoseUnit.MG_DL,
        mealContext: GlucoseMealContext? = null,
        notes: String = "", timestamp: Long = System.currentTimeMillis(),
    ) {
        viewModelScope.launch {
            repository.insertBloodGlucose(BloodGlucoseEntry(glucoseLevel = glucoseLevel, unit = unit, mealContext = mealContext, notes = notes, timestamp = timestamp))
        }
    }
    fun updateBloodGlucose(entry: BloodGlucoseEntry) {
        viewModelScope.launch { repository.updateBloodGlucose(entry) }
    }
    fun deleteBloodGlucose(entry: BloodGlucoseEntry) {
        viewModelScope.launch { repository.deleteBloodGlucose(entry) }
    }
    fun loadBloodGlucoseForEditing(id: Long) {
        viewModelScope.launch { _editingBloodGlucose.value = repository.getBloodGlucoseById(id) }
    }

    fun clearEditingBloodPressure() { _editingBloodPressure.value = null }
    fun clearEditingCholesterol() { _editingCholesterol.value = null }
    fun clearEditingWeight() { _editingWeight.value = null }
    fun clearEditingSpO2() { _editingSpO2.value = null }
    fun clearEditingBloodGlucose() { _editingBloodGlucose.value = null }
}
