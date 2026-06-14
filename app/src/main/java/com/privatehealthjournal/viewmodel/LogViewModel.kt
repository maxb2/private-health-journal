package com.privatehealthjournal.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
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
import com.privatehealthjournal.data.entity.MedicationSetLog
import com.privatehealthjournal.data.entity.MedicationSetReminder
import com.privatehealthjournal.data.entity.MedicationSetWithItems
import com.privatehealthjournal.data.entity.OtherEntry
import com.privatehealthjournal.data.entity.OtherEntryType
import com.privatehealthjournal.data.entity.CycleEntry
import com.privatehealthjournal.data.entity.GlucoseMealContext
import com.privatehealthjournal.data.entity.GlucoseUnit
import com.privatehealthjournal.data.entity.SpO2Entry
import com.privatehealthjournal.data.entity.StepCountEntry
import com.privatehealthjournal.data.entity.StepSource
import com.privatehealthjournal.data.entity.SymptomEntry
import com.privatehealthjournal.data.entity.Tag
import com.privatehealthjournal.data.entity.WeightEntry
import com.privatehealthjournal.data.entity.WeightUnit
import com.privatehealthjournal.data.preferences.AppPreferences
import com.privatehealthjournal.data.preferences.BudgetPreferences
import com.privatehealthjournal.data.preferences.appDataStore
import com.privatehealthjournal.data.repository.LogRepository
import com.privatehealthjournal.notification.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LogViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LogRepository

    val allMeals: StateFlow<List<MealWithDetails>>
    val allSymptomEntries: StateFlow<List<SymptomEntry>>
    val allBowelMovements: StateFlow<List<BowelMovementEntry>>
    val allMedications: StateFlow<List<MedicationEntry>>
    val allMedicationSets: StateFlow<List<MedicationSetWithItems>>
    val allRemindersBySet: StateFlow<Map<Long, List<MedicationSetReminder>>>
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
    val moodDescriptions: StateFlow<List<String>>
    val otherCategories: StateFlow<List<String>>
    val dailyBudget: StateFlow<Int?>
    val allCycleEntries: StateFlow<List<CycleEntry>>
    val recentCycleEntries: StateFlow<List<CycleEntry>>
    val showCycleTracking: StateFlow<Boolean>
    val allStepCountEntries: StateFlow<List<StepCountEntry>>
    val recentStepCountEntries: StateFlow<List<StepCountEntry>>
    val showStepCounting: StateFlow<Boolean>
    val stepSensorEnabled: StateFlow<Boolean>
    val healthConnectEnabled: StateFlow<Boolean>
    val stepsPerPointCredit: StateFlow<Int?>

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
            database.medicationSetDao(),
            database.medicationSetReminderDao(),
            database.medicationSetLogDao(),
            database.cycleEntryDao(),
            database.stepCountDao(),
            transaction = { block -> database.withTransaction { block() } }
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

        allRemindersBySet = repository.getAllReminders()
            .map { reminders -> reminders.groupBy { it.setId } }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

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

        moodDescriptions = repository.getDistinctOtherSubTypes(OtherEntryType.MOOD)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        otherCategories = repository.getDistinctOtherSubTypes(OtherEntryType.OTHER)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        dailyBudget = BudgetPreferences.getDailyBudget(application)
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

        allCycleEntries = repository.allCycleEntries
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        recentCycleEntries = repository.getRecentCycleEntries(5)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        showCycleTracking = AppPreferences.getShowCycleTracking(application)
            .stateIn(viewModelScope, SharingStarted.Lazily, true)

        allStepCountEntries = repository.allStepCountEntries
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        recentStepCountEntries = repository.getRecentStepCountEntries(5)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        showStepCounting = AppPreferences.getShowStepCounting(application)
            .stateIn(viewModelScope, SharingStarted.Lazily, false)

        stepSensorEnabled = AppPreferences.getStepSensorEnabled(application)
            .stateIn(viewModelScope, SharingStarted.Lazily, false)

        healthConnectEnabled = AppPreferences.getHealthConnectEnabled(application)
            .stateIn(viewModelScope, SharingStarted.Lazily, false)

        stepsPerPointCredit = BudgetPreferences.getStepsPerPointCredit(application)
            .stateIn(viewModelScope, SharingStarted.Lazily, null)
    }

    fun addMeal(
        mealType: MealType,
        foods: List<String>,
        tags: List<String>,
        notes: String = "",
        timestamp: Long = System.currentTimeMillis(),
        pointCost: Int? = null
    ) {
        viewModelScope.launch {
            repository.insertMeal(mealType, foods, tags, notes, timestamp, pointCost)
        }
    }

    fun saveDailyBudget(budget: Int?) {
        viewModelScope.launch {
            if (budget == null) {
                BudgetPreferences.clearDailyBudget(getApplication())
            } else {
                BudgetPreferences.setDailyBudget(getApplication(), budget)
            }
        }
    }

    fun setShowCycleTracking(show: Boolean) {
        viewModelScope.launch {
            AppPreferences.setShowCycleTracking(getApplication(), show)
        }
    }

    fun addCycleEntry(entry: CycleEntry) {
        viewModelScope.launch { repository.insertCycleEntry(entry) }
    }

    fun updateCycleEntry(entry: CycleEntry) {
        viewModelScope.launch { repository.updateCycleEntry(entry) }
    }

    fun deleteCycleEntry(entry: CycleEntry) {
        viewModelScope.launch { repository.deleteCycleEntry(entry) }
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

    private val _editingCycleEntry = MutableStateFlow<CycleEntry?>(null)
    val editingCycleEntry: StateFlow<CycleEntry?> = _editingCycleEntry.asStateFlow()

    private val _editingStepCount = MutableStateFlow<StepCountEntry?>(null)
    val editingStepCount: StateFlow<StepCountEntry?> = _editingStepCount.asStateFlow()

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

    fun loadCycleEntryForEditing(id: Long) {
        viewModelScope.launch {
            _editingCycleEntry.value = repository.getCycleEntryById(id)
        }
    }

    fun loadStepCountForEditing(id: Long) {
        viewModelScope.launch {
            _editingStepCount.value = repository.getStepCountById(id)
        }
    }

    // Step count methods
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
        _editingCycleEntry.value = null
        _editingStepCount.value = null
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
            repository.logMedicationSetAtomically(
                setId = setWithItems.set.id,
                items = setWithItems.items.map {
                    LogRepository.MedicationSetItemSpec(it.name, it.dosage)
                },
                timestamp = timestamp,
                notes = notes
            )
            // Dismiss any pending notification for this set
            ReminderScheduler.dismissNotification(getApplication(), setWithItems.set.id)
        }
    }

    // Reminder methods
    fun addReminder(reminder: MedicationSetReminder) {
        viewModelScope.launch {
            val id = repository.insertReminder(reminder)
            val saved = reminder.copy(id = id)
            ReminderScheduler.scheduleReminder(getApplication(), saved)
        }
    }

    fun updateReminder(reminder: MedicationSetReminder) {
        viewModelScope.launch {
            repository.updateReminder(reminder)
            if (reminder.enabled) {
                ReminderScheduler.scheduleReminder(getApplication(), reminder)
            } else {
                ReminderScheduler.cancelReminder(getApplication(), reminder.id)
            }
        }
    }

    fun deleteReminder(reminder: MedicationSetReminder) {
        viewModelScope.launch {
            ReminderScheduler.cancelReminder(getApplication(), reminder.id)
            repository.deleteReminder(reminder)
        }
    }

    // Export/Import
    fun exportData(uri: Uri, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                // Read fresh from the repository Flows. The StateFlow mirrors use
                // SharingStarted.Lazily, so they may still be empty if no screen has
                // subscribed before the user triggers an export.
                val meals = repository.allMeals.first()
                val symptoms = repository.allSymptomEntries.first()
                val bowelMovements = repository.allBowelMovements.first()
                val medications = repository.allMedications.first()
                val otherEntries = repository.allOtherEntries.first()
                val bloodPressure = repository.allBloodPressureEntries.first()
                val cholesterol = repository.allCholesterolEntries.first()
                val weight = repository.allWeightEntries.first()
                val spO2 = repository.allSpO2Entries.first()
                val bloodGlucose = repository.allBloodGlucoseEntries.first()
                val medicationSets = repository.allMedicationSets.first()
                val cycleEntries = repository.allCycleEntries.first()
                val stepCountEntries = repository.allStepCountEntries.first()
                val remindersBySetId = repository.getAllReminders().first()
                    .groupBy { it.setId }
                val logsBySetId = repository.getAllMedicationSetLogs().first()
                    .groupBy { it.setId }

                val json = withContext(Dispatchers.IO) {
                    DataExporter.export(
                        meals = meals,
                        symptoms = symptoms,
                        bowelMovements = bowelMovements,
                        medications = medications,
                        otherEntries = otherEntries,
                        bloodPressureEntries = bloodPressure,
                        cholesterolEntries = cholesterol,
                        weightEntries = weight,
                        spO2Entries = spO2,
                        bloodGlucoseEntries = bloodGlucose,
                        medicationSets = medicationSets,
                        remindersBySetId = remindersBySetId,
                        logsBySetId = logsBySetId,
                        cycleEntries = cycleEntries,
                        stepCountEntries = stepCountEntries
                    )
                }
                withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver.openOutputStream(uri)
                        ?.use { stream -> stream.write(json.toByteArray()) }
                        ?: throw IllegalStateException("Could not open output stream")
                }
                val total = meals.size + symptoms.size + bowelMovements.size +
                    medications.size + otherEntries.size +
                    bloodPressure.size + cholesterol.size + weight.size + spO2.size +
                    bloodGlucose.size + medicationSets.size +
                    cycleEntries.size + stepCountEntries.size
                onResult(true, "Exported $total entries successfully")
            } catch (e: Exception) {
                onResult(false, "Export failed: ${e.message}")
            }
        }
    }

    fun importData(uri: Uri, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val json = withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver.openInputStream(uri)?.use { stream ->
                        readJsonCapped(stream, MAX_IMPORT_BYTES)
                    }
                } ?: run {
                    onResult(false, "Could not read file")
                    return@launch
                }

                when (val result = DataImporter.import(json, repository)) {
                    is ImportResult.Success -> {
                        if (result.medicationSetRemindersImported > 0) {
                            ReminderScheduler.rescheduleAllReminders(getApplication())
                        }
                        val parts = buildList {
                            add("${result.mealsImported} meals")
                            add("${result.symptomsImported} symptoms")
                            add("${result.bowelMovementsImported} bowel movements")
                            add("${result.medicationsImported} medications")
                            add("${result.otherEntriesImported} other")
                            add("${result.bloodPressureImported} BP")
                            add("${result.cholesterolImported} cholesterol")
                            add("${result.weightImported} weight")
                            add("${result.spO2Imported} SpO2")
                            add("${result.bloodGlucoseImported} glucose")
                            add("${result.medicationSetsImported} med sets")
                            add("${result.cycleEntriesImported} cycle")
                            add("${result.stepCountEntriesImported} steps")
                        }
                        onResult(true, "Imported ${result.totalImported} entries: " + parts.joinToString(", "))
                    }
                    is ImportResult.Error -> {
                        onResult(false, result.message)
                    }
                }
            } catch (e: ImportTooLargeException) {
                onResult(false, "Import file too large (max ${MAX_IMPORT_BYTES / 1024 / 1024} MB)")
            } catch (e: Exception) {
                onResult(false, "Import failed: ${e.message}")
            }
        }
    }

    private class ImportTooLargeException : RuntimeException()

    private fun readJsonCapped(stream: java.io.InputStream, max: Long): String {
        val out = java.io.ByteArrayOutputStream()
        val buf = ByteArray(8 * 1024)
        var total = 0L
        while (true) {
            val read = stream.read(buf)
            if (read == -1) break
            total += read
            if (total > max) throw ImportTooLargeException()
            out.write(buf, 0, read)
        }
        return out.toString(Charsets.UTF_8.name())
    }

    companion object {
        private const val MAX_IMPORT_BYTES: Long = 50L * 1024 * 1024
    }
}
