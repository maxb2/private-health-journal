package com.foodsymptomlog.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.foodsymptomlog.data.AppDatabase
import com.foodsymptomlog.data.export.DataExporter
import com.foodsymptomlog.data.export.DataImporter
import com.foodsymptomlog.data.export.ImportResult
import com.foodsymptomlog.data.entity.BloodPressureEntry
import com.foodsymptomlog.data.entity.BowelMovementEntry
import com.foodsymptomlog.data.entity.CholesterolEntry
import com.foodsymptomlog.data.entity.MealEntry
import com.foodsymptomlog.data.entity.MealType
import com.foodsymptomlog.data.entity.MealWithDetails
import com.foodsymptomlog.data.entity.MedicationEntry
import com.foodsymptomlog.data.entity.OtherEntry
import com.foodsymptomlog.data.entity.SymptomEntry
import com.foodsymptomlog.data.entity.Tag
import com.foodsymptomlog.data.entity.WeightEntry
import com.foodsymptomlog.data.entity.WeightUnit
import com.foodsymptomlog.data.repository.LogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LogViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LogRepository

    val allMeals: StateFlow<List<MealWithDetails>>
    val allSymptomEntries: StateFlow<List<SymptomEntry>>
    val allBowelMovements: StateFlow<List<BowelMovementEntry>>
    val allMedications: StateFlow<List<MedicationEntry>>
    val allOtherEntries: StateFlow<List<OtherEntry>>
    val allBloodPressureEntries: StateFlow<List<BloodPressureEntry>>
    val allCholesterolEntries: StateFlow<List<CholesterolEntry>>
    val allWeightEntries: StateFlow<List<WeightEntry>>
    val ongoingSymptoms: StateFlow<List<SymptomEntry>>
    val recentMeals: StateFlow<List<MealWithDetails>>
    val recentSymptomEntries: StateFlow<List<SymptomEntry>>
    val recentBowelMovements: StateFlow<List<BowelMovementEntry>>
    val recentMedications: StateFlow<List<MedicationEntry>>
    val recentOtherEntries: StateFlow<List<OtherEntry>>
    val recentBloodPressureEntries: StateFlow<List<BloodPressureEntry>>
    val recentCholesterolEntries: StateFlow<List<CholesterolEntry>>
    val recentWeightEntries: StateFlow<List<WeightEntry>>
    val allTags: StateFlow<List<Tag>>
    val allMedicationNames: StateFlow<List<String>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = LogRepository(
            database.mealDao(),
            database.symptomEntryDao(),
            database.bowelMovementDao(),
            database.medicationDao(),
            database.otherEntryDao(),
            database.bloodPressureDao(),
            database.cholesterolDao(),
            database.weightDao()
        )

        allMeals = repository.allMeals
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        allSymptomEntries = repository.allSymptomEntries
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        allBowelMovements = repository.allBowelMovements
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        allMedications = repository.allMedications
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        allOtherEntries = repository.allOtherEntries
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        allBloodPressureEntries = repository.allBloodPressureEntries
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        allCholesterolEntries = repository.allCholesterolEntries
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        allWeightEntries = repository.allWeightEntries
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        ongoingSymptoms = repository.ongoingSymptoms
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        recentMeals = repository.getRecentMeals(5)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        recentSymptomEntries = repository.getRecentSymptomEntries(5)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        recentBowelMovements = repository.getRecentBowelMovements(5)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        recentMedications = repository.getRecentMedications(5)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        recentOtherEntries = repository.getRecentOtherEntries(5)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        recentBloodPressureEntries = repository.getRecentBloodPressureEntries(5)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        recentCholesterolEntries = repository.getRecentCholesterolEntries(5)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        recentWeightEntries = repository.getRecentWeightEntries(5)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        allTags = repository.allTags
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        allMedicationNames = repository.allMedicationNames
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    fun addMeal(
        mealType: MealType,
        foods: List<String>,
        tags: List<String>,
        notes: String = "",
        timestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            repository.insertMeal(mealType, foods, tags, notes, timestamp)
        }
    }

    fun addSymptom(
        name: String,
        severity: Int,
        startTime: Long = System.currentTimeMillis(),
        endTime: Long? = null,
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.insertSymptom(
                SymptomEntry(
                    name = name,
                    severity = severity,
                    startTime = startTime,
                    endTime = endTime,
                    notes = notes
                )
            )
        }
    }

    fun endSymptom(symptomEntry: SymptomEntry) {
        viewModelScope.launch {
            repository.endSymptom(symptomEntry.id)
        }
    }

    fun addBowelMovement(
        bristolType: Int,
        notes: String = "",
        timestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            repository.insertBowelMovement(
                BowelMovementEntry(bristolType = bristolType, notes = notes, timestamp = timestamp)
            )
        }
    }

    fun deleteMeal(meal: MealWithDetails) {
        viewModelScope.launch {
            repository.deleteMeal(meal.meal)
        }
    }

    fun deleteSymptom(symptomEntry: SymptomEntry) {
        viewModelScope.launch {
            repository.deleteSymptom(symptomEntry)
        }
    }

    fun deleteBowelMovement(entry: BowelMovementEntry) {
        viewModelScope.launch {
            repository.deleteBowelMovement(entry)
        }
    }

    // Medication methods
    fun addMedication(
        name: String,
        dosage: String = "",
        notes: String = "",
        timestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            repository.insertMedication(
                MedicationEntry(name = name, dosage = dosage, notes = notes, timestamp = timestamp)
            )
        }
    }

    fun deleteMedication(entry: MedicationEntry) {
        viewModelScope.launch {
            repository.deleteMedication(entry)
        }
    }

    fun updateMedication(entry: MedicationEntry) {
        viewModelScope.launch {
            repository.updateMedication(entry)
        }
    }

    // Other entry methods
    fun addOtherEntry(entry: OtherEntry) {
        viewModelScope.launch {
            repository.insertOtherEntry(entry)
        }
    }

    fun deleteOtherEntry(entry: OtherEntry) {
        viewModelScope.launch {
            repository.deleteOtherEntry(entry)
        }
    }

    fun updateOtherEntry(entry: OtherEntry) {
        viewModelScope.launch {
            repository.updateOtherEntry(entry)
        }
    }

    // Blood Pressure methods
    fun addBloodPressure(
        systolic: Int,
        diastolic: Int,
        pulse: Int? = null,
        notes: String = "",
        timestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            repository.insertBloodPressure(
                BloodPressureEntry(
                    systolic = systolic,
                    diastolic = diastolic,
                    pulse = pulse,
                    notes = notes,
                    timestamp = timestamp
                )
            )
        }
    }

    fun updateBloodPressure(entry: BloodPressureEntry) {
        viewModelScope.launch {
            repository.updateBloodPressure(entry)
        }
    }

    fun deleteBloodPressure(entry: BloodPressureEntry) {
        viewModelScope.launch {
            repository.deleteBloodPressure(entry)
        }
    }

    // Cholesterol methods
    fun addCholesterol(
        total: Int? = null,
        ldl: Int? = null,
        hdl: Int? = null,
        triglycerides: Int? = null,
        notes: String = "",
        timestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            repository.insertCholesterol(
                CholesterolEntry(
                    total = total,
                    ldl = ldl,
                    hdl = hdl,
                    triglycerides = triglycerides,
                    notes = notes,
                    timestamp = timestamp
                )
            )
        }
    }

    fun updateCholesterol(entry: CholesterolEntry) {
        viewModelScope.launch {
            repository.updateCholesterol(entry)
        }
    }

    fun deleteCholesterol(entry: CholesterolEntry) {
        viewModelScope.launch {
            repository.deleteCholesterol(entry)
        }
    }

    // Weight methods
    fun addWeight(
        weight: Double,
        unit: WeightUnit = WeightUnit.LB,
        notes: String = "",
        timestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            repository.insertWeight(
                WeightEntry(
                    weight = weight,
                    unit = unit,
                    notes = notes,
                    timestamp = timestamp
                )
            )
        }
    }

    fun updateWeight(entry: WeightEntry) {
        viewModelScope.launch {
            repository.updateWeight(entry)
        }
    }

    fun deleteWeight(entry: WeightEntry) {
        viewModelScope.launch {
            repository.deleteWeight(entry)
        }
    }

    // Update methods
    fun updateSymptom(symptomEntry: SymptomEntry) {
        viewModelScope.launch {
            repository.updateSymptom(symptomEntry)
        }
    }

    fun updateBowelMovement(entry: BowelMovementEntry) {
        viewModelScope.launch {
            repository.updateBowelMovement(entry)
        }
    }

    fun updateMeal(
        meal: MealEntry,
        foods: List<String>,
        tags: List<String>
    ) {
        viewModelScope.launch {
            repository.updateMeal(meal, foods, tags)
        }
    }

    // Get by ID methods (for editing)
    private val _editingSymptom = MutableStateFlow<SymptomEntry?>(null)
    val editingSymptom: StateFlow<SymptomEntry?> = _editingSymptom.asStateFlow()

    private val _editingBowelMovement = MutableStateFlow<BowelMovementEntry?>(null)
    val editingBowelMovement: StateFlow<BowelMovementEntry?> = _editingBowelMovement.asStateFlow()

    private val _editingMeal = MutableStateFlow<MealWithDetails?>(null)
    val editingMeal: StateFlow<MealWithDetails?> = _editingMeal.asStateFlow()

    private val _editingMedication = MutableStateFlow<MedicationEntry?>(null)
    val editingMedication: StateFlow<MedicationEntry?> = _editingMedication.asStateFlow()

    private val _editingOtherEntry = MutableStateFlow<OtherEntry?>(null)
    val editingOtherEntry: StateFlow<OtherEntry?> = _editingOtherEntry.asStateFlow()

    private val _editingBloodPressure = MutableStateFlow<BloodPressureEntry?>(null)
    val editingBloodPressure: StateFlow<BloodPressureEntry?> = _editingBloodPressure.asStateFlow()

    private val _editingCholesterol = MutableStateFlow<CholesterolEntry?>(null)
    val editingCholesterol: StateFlow<CholesterolEntry?> = _editingCholesterol.asStateFlow()

    private val _editingWeight = MutableStateFlow<WeightEntry?>(null)
    val editingWeight: StateFlow<WeightEntry?> = _editingWeight.asStateFlow()

    fun loadSymptomForEditing(id: Long) {
        viewModelScope.launch {
            _editingSymptom.value = repository.getSymptomById(id)
        }
    }

    fun loadBowelMovementForEditing(id: Long) {
        viewModelScope.launch {
            _editingBowelMovement.value = repository.getBowelMovementById(id)
        }
    }

    fun loadMealForEditing(id: Long) {
        viewModelScope.launch {
            _editingMeal.value = repository.getMealWithDetailsById(id)
        }
    }

    fun loadMedicationForEditing(id: Long) {
        viewModelScope.launch {
            _editingMedication.value = repository.getMedicationById(id)
        }
    }

    fun loadOtherEntryForEditing(id: Long) {
        viewModelScope.launch {
            _editingOtherEntry.value = repository.getOtherEntryById(id)
        }
    }

    fun loadBloodPressureForEditing(id: Long) {
        viewModelScope.launch {
            _editingBloodPressure.value = repository.getBloodPressureById(id)
        }
    }

    fun loadCholesterolForEditing(id: Long) {
        viewModelScope.launch {
            _editingCholesterol.value = repository.getCholesterolById(id)
        }
    }

    fun loadWeightForEditing(id: Long) {
        viewModelScope.launch {
            _editingWeight.value = repository.getWeightById(id)
        }
    }

    fun clearEditingState() {
        _editingSymptom.value = null
        _editingBowelMovement.value = null
        _editingMeal.value = null
        _editingMedication.value = null
        _editingOtherEntry.value = null
        _editingBloodPressure.value = null
        _editingCholesterol.value = null
        _editingWeight.value = null
    }

    // Export/Import
    fun exportData(uri: Uri, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val json = DataExporter.export(
                    meals = allMeals.value,
                    symptoms = allSymptomEntries.value,
                    medications = allMedications.value,
                    otherEntries = allOtherEntries.value,
                    bloodPressureEntries = allBloodPressureEntries.value,
                    cholesterolEntries = allCholesterolEntries.value,
                    weightEntries = allWeightEntries.value
                )
                getApplication<Application>().contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(json.toByteArray())
                }
                val total = allMeals.value.size + allSymptomEntries.value.size +
                    allMedications.value.size + allOtherEntries.value.size +
                    allBloodPressureEntries.value.size + allCholesterolEntries.value.size +
                    allWeightEntries.value.size
                onResult(true, "Exported $total entries successfully")
            } catch (e: Exception) {
                onResult(false, "Export failed: ${e.message}")
            }
        }
    }

    fun importData(uri: Uri, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val json = getApplication<Application>().contentResolver.openInputStream(uri)?.use { stream ->
                    stream.bufferedReader().readText()
                } ?: run {
                    onResult(false, "Could not read file")
                    return@launch
                }

                when (val result = DataImporter.import(json, repository)) {
                    is ImportResult.Success -> {
                        onResult(true, "Imported ${result.totalImported} entries: " +
                            "${result.mealsImported} meals, ${result.symptomsImported} symptoms, " +
                            "${result.medicationsImported} medications, ${result.otherEntriesImported} other")
                    }
                    is ImportResult.Error -> {
                        onResult(false, result.message)
                    }
                }
            } catch (e: Exception) {
                onResult(false, "Import failed: ${e.message}")
            }
        }
    }
}
