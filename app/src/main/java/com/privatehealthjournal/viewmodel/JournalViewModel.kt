package com.privatehealthjournal.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.privatehealthjournal.data.entity.BowelMovementEntry
import com.privatehealthjournal.data.entity.CycleEntry
import com.privatehealthjournal.data.entity.MealEntry
import com.privatehealthjournal.data.entity.MealType
import com.privatehealthjournal.data.entity.MealWithDetails
import com.privatehealthjournal.data.entity.OtherEntry
import com.privatehealthjournal.data.entity.OtherEntryType
import com.privatehealthjournal.data.entity.SymptomEntry
import com.privatehealthjournal.data.entity.Tag
import com.privatehealthjournal.data.preferences.AppPreferences
import com.privatehealthjournal.data.preferences.BudgetPreferences
import com.privatehealthjournal.data.repository.JournalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JournalViewModel(
    private val repository: JournalRepository,
    application: Application,
) : AndroidViewModel(application) {

    val allMeals: StateFlow<List<MealWithDetails>> = repository.allMeals
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val recentMeals: StateFlow<List<MealWithDetails>> = repository.getRecentMeals(5)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allTags: StateFlow<List<Tag>> = repository.allTags
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allFoodNames: StateFlow<List<String>> = repository.allFoodNames
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allSymptomEntries: StateFlow<List<SymptomEntry>> = repository.allSymptomEntries
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val recentSymptomEntries: StateFlow<List<SymptomEntry>> = repository.getRecentSymptomEntries(5)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val ongoingSymptoms: StateFlow<List<SymptomEntry>> = repository.ongoingSymptoms
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allSymptomNames: StateFlow<List<String>> = repository.allSymptomNames
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allBowelMovements: StateFlow<List<BowelMovementEntry>> = repository.allBowelMovements
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val recentBowelMovements: StateFlow<List<BowelMovementEntry>> = repository.getRecentBowelMovements(5)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allOtherEntries: StateFlow<List<OtherEntry>> = repository.allOtherEntries
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val recentOtherEntries: StateFlow<List<OtherEntry>> = repository.getRecentOtherEntries(5)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val exerciseTypes: StateFlow<List<String>> = repository.getDistinctOtherSubTypes(OtherEntryType.EXERCISE)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val sleepQualities: StateFlow<List<String>> = repository.getDistinctOtherSubTypes(OtherEntryType.SLEEP)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val stressSources: StateFlow<List<String>> = repository.getDistinctOtherSubTypes(OtherEntryType.STRESS)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val moodDescriptions: StateFlow<List<String>> = repository.getDistinctOtherSubTypes(OtherEntryType.MOOD)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val otherCategories: StateFlow<List<String>> = repository.getDistinctOtherSubTypes(OtherEntryType.OTHER)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allCycleEntries: StateFlow<List<CycleEntry>> = repository.allCycleEntries
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val recentCycleEntries: StateFlow<List<CycleEntry>> = repository.getRecentCycleEntries(5)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val dailyBudget: StateFlow<Int?> = BudgetPreferences.getDailyBudget(application)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)
    val showCycleTracking: StateFlow<Boolean> = AppPreferences.getShowCycleTracking(application)
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    fun saveDailyBudget(budget: Int?) {
        viewModelScope.launch {
            if (budget == null) BudgetPreferences.clearDailyBudget(getApplication())
            else BudgetPreferences.setDailyBudget(getApplication(), budget)
        }
    }
    fun setShowCycleTracking(show: Boolean) {
        viewModelScope.launch { AppPreferences.setShowCycleTracking(getApplication(), show) }
    }

    private val _editingMeal = MutableStateFlow<MealWithDetails?>(null)
    val editingMeal: StateFlow<MealWithDetails?> = _editingMeal.asStateFlow()
    private val _editingSymptom = MutableStateFlow<SymptomEntry?>(null)
    val editingSymptom: StateFlow<SymptomEntry?> = _editingSymptom.asStateFlow()
    private val _editingBowelMovement = MutableStateFlow<BowelMovementEntry?>(null)
    val editingBowelMovement: StateFlow<BowelMovementEntry?> = _editingBowelMovement.asStateFlow()
    private val _editingOtherEntry = MutableStateFlow<OtherEntry?>(null)
    val editingOtherEntry: StateFlow<OtherEntry?> = _editingOtherEntry.asStateFlow()
    private val _editingCycleEntry = MutableStateFlow<CycleEntry?>(null)
    val editingCycleEntry: StateFlow<CycleEntry?> = _editingCycleEntry.asStateFlow()

    // Meals
    fun addMeal(
        mealType: MealType, foods: List<String>, tags: List<String>,
        notes: String = "", timestamp: Long = System.currentTimeMillis(), pointCost: Int? = null,
    ) {
        viewModelScope.launch { repository.insertMeal(mealType, foods, tags, notes, timestamp, pointCost) }
    }
    fun updateMeal(meal: MealEntry, foods: List<String>, tags: List<String>) {
        viewModelScope.launch { repository.updateMeal(meal, foods, tags) }
    }
    fun deleteMeal(meal: MealWithDetails) {
        viewModelScope.launch { repository.deleteMeal(meal.meal) }
    }
    fun loadMealForEditing(id: Long) {
        viewModelScope.launch { _editingMeal.value = repository.getMealWithDetailsById(id) }
    }

    // Symptoms
    fun addSymptom(
        name: String, severity: Int,
        startTime: Long = System.currentTimeMillis(), endTime: Long? = null, notes: String = "",
    ) {
        viewModelScope.launch {
            repository.insertSymptom(SymptomEntry(name = name, severity = severity, startTime = startTime, endTime = endTime, notes = notes))
        }
    }
    fun updateSymptom(entry: SymptomEntry) {
        viewModelScope.launch { repository.updateSymptom(entry) }
    }
    fun deleteSymptom(entry: SymptomEntry) {
        viewModelScope.launch { repository.deleteSymptom(entry) }
    }
    fun endSymptom(entry: SymptomEntry) {
        viewModelScope.launch { repository.endSymptom(entry.id) }
    }
    fun loadSymptomForEditing(id: Long) {
        viewModelScope.launch { _editingSymptom.value = repository.getSymptomById(id) }
    }

    // Bowel movements
    fun addBowelMovement(bristolType: Int, notes: String = "", timestamp: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            repository.insertBowelMovement(BowelMovementEntry(bristolType = bristolType, notes = notes, timestamp = timestamp))
        }
    }
    fun updateBowelMovement(entry: BowelMovementEntry) {
        viewModelScope.launch { repository.updateBowelMovement(entry) }
    }
    fun deleteBowelMovement(entry: BowelMovementEntry) {
        viewModelScope.launch { repository.deleteBowelMovement(entry) }
    }
    fun loadBowelMovementForEditing(id: Long) {
        viewModelScope.launch { _editingBowelMovement.value = repository.getBowelMovementById(id) }
    }

    // Other
    fun addOtherEntry(entry: OtherEntry) {
        viewModelScope.launch { repository.insertOtherEntry(entry) }
    }
    fun updateOtherEntry(entry: OtherEntry) {
        viewModelScope.launch { repository.updateOtherEntry(entry) }
    }
    fun deleteOtherEntry(entry: OtherEntry) {
        viewModelScope.launch { repository.deleteOtherEntry(entry) }
    }
    fun loadOtherEntryForEditing(id: Long) {
        viewModelScope.launch { _editingOtherEntry.value = repository.getOtherEntryById(id) }
    }

    // Cycle
    fun addCycleEntry(entry: CycleEntry) {
        viewModelScope.launch { repository.insertCycleEntry(entry) }
    }
    fun updateCycleEntry(entry: CycleEntry) {
        viewModelScope.launch { repository.updateCycleEntry(entry) }
    }
    fun deleteCycleEntry(entry: CycleEntry) {
        viewModelScope.launch { repository.deleteCycleEntry(entry) }
    }
    fun loadCycleEntryForEditing(id: Long) {
        viewModelScope.launch { _editingCycleEntry.value = repository.getCycleEntryById(id) }
    }

    fun clearEditingMeal() { _editingMeal.value = null }
    fun clearEditingSymptom() { _editingSymptom.value = null }
    fun clearEditingBowelMovement() { _editingBowelMovement.value = null }
    fun clearEditingOtherEntry() { _editingOtherEntry.value = null }
    fun clearEditingCycleEntry() { _editingCycleEntry.value = null }
}
