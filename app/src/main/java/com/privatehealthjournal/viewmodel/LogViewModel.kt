package com.privatehealthjournal.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.privatehealthjournal.data.AppDatabase
import com.privatehealthjournal.data.export.DataExporter
import com.privatehealthjournal.data.export.DataImporter
import com.privatehealthjournal.data.export.ImportResult
import com.privatehealthjournal.data.entity.BloodGlucoseEntry
import com.privatehealthjournal.data.entity.BloodPressureEntry
import com.privatehealthjournal.data.entity.BowelMovementEntry
import com.privatehealthjournal.data.entity.CholesterolEntry
import com.privatehealthjournal.data.entity.MealEntry
import com.privatehealthjournal.data.entity.MealType
import com.privatehealthjournal.data.entity.MealWithDetails
import com.privatehealthjournal.data.entity.MedicationEntry
import com.privatehealthjournal.data.entity.MedicationSet
import com.privatehealthjournal.data.entity.MedicationSetWithItems
import com.privatehealthjournal.data.entity.OtherEntry
import com.privatehealthjournal.data.entity.OtherEntryType
import com.privatehealthjournal.data.entity.GlucoseMealContext
import com.privatehealthjournal.data.entity.GlucoseUnit
import com.privatehealthjournal.data.entity.SpO2Entry
import com.privatehealthjournal.data.entity.SymptomEntry
import com.privatehealthjournal.data.entity.Tag
import com.privatehealthjournal.data.entity.WeightEntry
import com.privatehealthjournal.data.entity.WeightUnit
import com.privatehealthjournal.data.repository.LogRepository
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
    val allMedicationSets: StateFlow<List<MedicationSetWithItems>>
    val allOtherEntries: StateFlow<List<OtherEntry>>
    val allBloodPressureEntries: StateFlow<List<BloodPressureEntry>>
    val allCholesterolEntries: StateFlow<List<CholesterolEntry>>
    val allWeightEntries: StateFlow<List<WeightEntry>>
    val allSpO2Entries: StateFlow<List<SpO2Entry>>
    val allBloodGlucoseEntries: StateFlow<List<BloodGlucoseEntry>>
    val ongoingSymptoms: StateFlow<List<SymptomEntry>>
    val recentMeals: StateFlow<List<MealWithDetails>>
    val recentSymptomEntries: StateFlow<List<SymptomEntry>>
    val recentBowelMovements: StateFlow<List<BowelMovementEntry>>
    val recentMedications: StateFlow<List<MedicationEntry>>
    val recentOtherEntries: StateFlow<List<OtherEntry>>
    val recentBloodPressureEntries: StateFlow<List<BloodPressureEntry>>
    val recentCholesterolEntries: StateFlow<List<CholesterolEntry>>
    val recentWeightEntries: StateFlow<List<WeightEntry>>
    val recentSpO2Entries: StateFlow<List<SpO2Entry>>
    val recentBloodGlucoseEntries: StateFlow<List<BloodGlucoseEntry>>
    val allTags: StateFlow<List<Tag>>
    val allMedicationNames: StateFlow<List<String>>
    val allFoodNames: StateFlow<List<String>>
    val allSymptomNames: StateFlow<List<String>>
    val exerciseTypes: StateFlow<List<String>>
    val sleepQualities: StateFlow<List<String>>
    val stressSources: StateFlow<List<String>>
    val otherCategories: StateFlow<List<String>>

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
            database.weightDao(),
            database.spO2Dao(),
            database.bloodGlucoseDao(),
            database.medicationSetDao()
        )

        allMeals = repository.allMeals
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        allSymptomEntries = repository.allSymptomEntries
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        allBowelMovements = repository.allBowelMovements
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        allMedications = repository.allMedications
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        allMedicationSets = repository.allMedicationSets
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        allOtherEntries = repository.allOtherEntries
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        allBloodPressureEntries = repository.allBloodPressureEntries
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        allCholesterolEntries = repository.allCholesterolEntries
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        allWeightEntries = repository.allWeightEntries
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        allSpO2Entries = repository.allSpO2Entries
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        allBloodGlucoseEntries = repository.allBloodGlucoseEntries
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

        recentSpO2Entries = repository.getRecentSpO2Entries(5)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        recentBloodGlucoseEntries = repository.getRecentBloodGlucoseEntries(5)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        allTags = repository.allTags
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        allMedicationNames = repository.allMedicationNames
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        allFoodNames = repository.allFoodNames
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        allSymptomNames = repository.allSymptomNames
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        exerciseTypes = repository.getDistinctOtherSubTypes(OtherEntryType.EXERCISE)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        sleepQualities = repository.getDistinctOtherSubTypes(OtherEntryType.SLEEP)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        stressSources = repository.getDistinctOtherSubTypes(OtherEntryType.STRESS)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        otherCategories = repository.getDistinctOtherSubTypes(OtherEntryType.OTHER)
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

    // SpO2 methods
    fun addSpO2(
        spo2: Int,
        pulse: Int? = null,
        notes: String = "",
        timestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            repository.insertSpO2(
                SpO2Entry(
                    spo2 = spo2,
                    pulse = pulse,
                    notes = notes,
                    timestamp = timestamp
                )
            )
        }
    }

    fun updateSpO2(entry: SpO2Entry) {
        viewModelScope.launch {
            repository.updateSpO2(entry)
        }
    }

    fun deleteSpO2(entry: SpO2Entry) {
        viewModelScope.launch {
            repository.deleteSpO2(entry)
        }
    }

    // Blood Glucose methods
    fun addBloodGlucose(
        glucoseLevel: Double,
        unit: GlucoseUnit = GlucoseUnit.MG_DL,
        mealContext: GlucoseMealContext? = null,
        notes: String = "",
        timestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            repository.insertBloodGlucose(
                BloodGlucoseEntry(
                    glucoseLevel = glucoseLevel,
                    unit = unit,
                    mealContext = mealContext,
                    notes = notes,
                    timestamp = timestamp
                )
            )
        }
    }

    fun updateBloodGlucose(entry: BloodGlucoseEntry) {
        viewModelScope.launch {
            repository.updateBloodGlucose(entry)
        }
    }

    fun deleteBloodGlucose(entry: BloodGlucoseEntry) {
        viewModelScope.launch {
            repository.deleteBloodGlucose(entry)
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

    private val _editingSpO2 = MutableStateFlow<SpO2Entry?>(null)
    val editingSpO2: StateFlow<SpO2Entry?> = _editingSpO2.asStateFlow()

    private val _editingBloodGlucose = MutableStateFlow<BloodGlucoseEntry?>(null)
    val editingBloodGlucose: StateFlow<BloodGlucoseEntry?> = _editingBloodGlucose.asStateFlow()

    private val _editingMedicationSet = MutableStateFlow<MedicationSetWithItems?>(null)
    val editingMedicationSet: StateFlow<MedicationSetWithItems?> = _editingMedicationSet.asStateFlow()

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

    fun loadSpO2ForEditing(id: Long) {
        viewModelScope.launch {
            _editingSpO2.value = repository.getSpO2ById(id)
        }
    }

    fun loadBloodGlucoseForEditing(id: Long) {
        viewModelScope.launch {
            _editingBloodGlucose.value = repository.getBloodGlucoseById(id)
        }
    }

    fun loadMedicationSetForEditing(id: Long) {
        viewModelScope.launch {
            _editingMedicationSet.value = repository.getMedicationSetWithItemsById(id)
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
        _editingSpO2.value = null
        _editingBloodGlucose.value = null
        _editingMedicationSet.value = null
    }

    // Medication Set methods
    fun addMedicationSet(name: String, items: List<Pair<String, String>>) {
        viewModelScope.launch {
            repository.insertMedicationSet(name, items)
        }
    }

    fun updateMedicationSet(id: Long, name: String, items: List<Pair<String, String>>) {
        viewModelScope.launch {
            repository.updateMedicationSet(MedicationSet(id = id, name = name), items)
        }
    }

    fun deleteMedicationSet(id: Long) {
        viewModelScope.launch {
            repository.deleteMedicationSetById(id)
        }
    }

    fun logMedicationSet(setWithItems: MedicationSetWithItems) {
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            val notes = "Logged from set: ${setWithItems.set.name}"
            setWithItems.items.forEach { item ->
                repository.insertMedication(
                    MedicationEntry(
                        name = item.name,
                        dosage = item.dosage,
                        notes = notes,
                        timestamp = timestamp
                    )
                )
            }
        }
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
                    weightEntries = allWeightEntries.value,
                    spO2Entries = allSpO2Entries.value,
                    bloodGlucoseEntries = allBloodGlucoseEntries.value,
                    medicationSets = allMedicationSets.value
                )
                getApplication<Application>().contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(json.toByteArray())
                }
                val total = allMeals.value.size + allSymptomEntries.value.size +
                    allMedications.value.size + allOtherEntries.value.size +
                    allBloodPressureEntries.value.size + allCholesterolEntries.value.size +
                    allWeightEntries.value.size + allSpO2Entries.value.size +
                    allBloodGlucoseEntries.value.size
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
