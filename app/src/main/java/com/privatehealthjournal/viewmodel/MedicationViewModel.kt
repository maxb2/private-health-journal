package com.privatehealthjournal.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.privatehealthjournal.data.entity.MedicationEntry
import com.privatehealthjournal.data.entity.MedicationSet
import com.privatehealthjournal.data.entity.MedicationSetReminder
import com.privatehealthjournal.data.entity.MedicationSetWithItems
import com.privatehealthjournal.data.repository.MedicationRepository
import com.privatehealthjournal.notification.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MedicationViewModel(
    private val repository: MedicationRepository,
    application: Application,
) : AndroidViewModel(application) {

    val allMedications: StateFlow<List<MedicationEntry>> = repository.allMedications
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allMedicationNames: StateFlow<List<String>> = repository.allMedicationNames
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allMedicationSets: StateFlow<List<MedicationSetWithItems>> = repository.allMedicationSets
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allRemindersBySet: StateFlow<Map<Long, List<MedicationSetReminder>>> = repository.getAllReminders()
        .map { reminders -> reminders.groupBy { it.setId } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())
    val recentMedications: StateFlow<List<MedicationEntry>> = repository.getRecentMedications(5)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _editingMedication = MutableStateFlow<MedicationEntry?>(null)
    val editingMedication: StateFlow<MedicationEntry?> = _editingMedication.asStateFlow()

    private val _editingMedicationSet = MutableStateFlow<MedicationSetWithItems?>(null)
    val editingMedicationSet: StateFlow<MedicationSetWithItems?> = _editingMedicationSet.asStateFlow()

    fun addMedication(
        name: String, dosage: String = "", notes: String = "",
        timestamp: Long = System.currentTimeMillis(),
    ) {
        viewModelScope.launch {
            repository.insertMedication(MedicationEntry(name = name, dosage = dosage, notes = notes, timestamp = timestamp))
        }
    }
    fun updateMedication(entry: MedicationEntry) {
        viewModelScope.launch { repository.updateMedication(entry) }
    }
    fun deleteMedication(entry: MedicationEntry) {
        viewModelScope.launch { repository.deleteMedication(entry) }
    }
    fun loadMedicationForEditing(id: Long) {
        viewModelScope.launch { _editingMedication.value = repository.getMedicationById(id) }
    }

    fun addMedicationSet(name: String, items: List<Pair<String, String>>) {
        viewModelScope.launch { repository.insertMedicationSet(name, items) }
    }
    fun updateMedicationSet(id: Long, name: String, items: List<Pair<String, String>>) {
        viewModelScope.launch { repository.updateMedicationSet(MedicationSet(id = id, name = name), items) }
    }
    fun deleteMedicationSet(id: Long) {
        viewModelScope.launch { repository.deleteMedicationSetById(id) }
    }
    fun loadMedicationSetForEditing(id: Long) {
        viewModelScope.launch { _editingMedicationSet.value = repository.getMedicationSetWithItemsById(id) }
    }

    fun logMedicationSet(setWithItems: MedicationSetWithItems) {
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            val notes = "Logged from set: ${setWithItems.set.name}"
            repository.logMedicationSetAtomically(
                setId = setWithItems.set.id,
                items = setWithItems.items.map {
                    MedicationRepository.MedicationSetItemSpec(it.name, it.dosage)
                },
                timestamp = timestamp,
                notes = notes,
            )
            ReminderScheduler.dismissNotification(getApplication(), setWithItems.set.id)
        }
    }

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

    fun clearEditingMedication() { _editingMedication.value = null }
    fun clearEditingMedicationSet() { _editingMedicationSet.value = null }
}
