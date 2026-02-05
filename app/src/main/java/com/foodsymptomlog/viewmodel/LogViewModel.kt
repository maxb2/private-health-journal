package com.foodsymptomlog.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.foodsymptomlog.data.AppDatabase
import com.foodsymptomlog.data.entity.BowelMovementEntry
import com.foodsymptomlog.data.entity.MealType
import com.foodsymptomlog.data.entity.MealWithDetails
import com.foodsymptomlog.data.entity.SymptomEntry
import com.foodsymptomlog.data.entity.Tag
import com.foodsymptomlog.data.repository.LogRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LogViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LogRepository

    val allMeals: StateFlow<List<MealWithDetails>>
    val allSymptomEntries: StateFlow<List<SymptomEntry>>
    val allBowelMovements: StateFlow<List<BowelMovementEntry>>
    val ongoingSymptoms: StateFlow<List<SymptomEntry>>
    val recentMeals: StateFlow<List<MealWithDetails>>
    val recentSymptomEntries: StateFlow<List<SymptomEntry>>
    val recentBowelMovements: StateFlow<List<BowelMovementEntry>>
    val allTags: StateFlow<List<Tag>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = LogRepository(
            database.mealDao(),
            database.symptomEntryDao(),
            database.bowelMovementDao()
        )

        allMeals = repository.allMeals
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        allSymptomEntries = repository.allSymptomEntries
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        allBowelMovements = repository.allBowelMovements
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        ongoingSymptoms = repository.ongoingSymptoms
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        recentMeals = repository.getRecentMeals(5)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        recentSymptomEntries = repository.getRecentSymptomEntries(5)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        recentBowelMovements = repository.getRecentBowelMovements(5)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        allTags = repository.allTags
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    fun addMeal(
        mealType: MealType,
        foods: List<String>,
        tags: List<String>,
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.insertMeal(mealType, foods, tags, notes)
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

    fun addBowelMovement(bristolType: Int, notes: String = "") {
        viewModelScope.launch {
            repository.insertBowelMovement(BowelMovementEntry(bristolType = bristolType, notes = notes))
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
}
