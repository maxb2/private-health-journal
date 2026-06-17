package com.privatehealthjournal.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.privatehealthjournal.data.entity.BloodPressureEntry
import com.privatehealthjournal.data.entity.CholesterolEntry
import com.privatehealthjournal.data.entity.CycleEntry
import com.privatehealthjournal.data.entity.MealWithDetails
import com.privatehealthjournal.data.entity.MedicationEntry
import com.privatehealthjournal.data.entity.OtherEntry
import com.privatehealthjournal.data.entity.BloodGlucoseEntry
import com.privatehealthjournal.data.entity.SpO2Entry
import com.privatehealthjournal.data.entity.StepCountEntry
import com.privatehealthjournal.data.entity.SymptomEntry
import com.privatehealthjournal.data.entity.WeightEntry
import com.privatehealthjournal.ui.components.BloodGlucoseCard
import com.privatehealthjournal.ui.components.BloodPressureCard
import com.privatehealthjournal.ui.components.CholesterolCard
import com.privatehealthjournal.ui.components.CycleEntryCard
import com.privatehealthjournal.ui.components.MealEntryCard
import com.privatehealthjournal.ui.components.MedicationCard
import com.privatehealthjournal.ui.components.OtherEntryCard
import com.privatehealthjournal.ui.components.SpO2Card
import com.privatehealthjournal.ui.components.StepCountCard
import com.privatehealthjournal.ui.components.SymptomEntryCard
import com.privatehealthjournal.ui.components.WeightCard
import com.privatehealthjournal.ui.nav.NavIntent
import com.privatehealthjournal.viewmodel.BiometricsViewModel
import com.privatehealthjournal.viewmodel.JournalViewModel
import com.privatehealthjournal.viewmodel.MedicationViewModel
import com.privatehealthjournal.viewmodel.StepsViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class FilterType {
    ALL, MEALS, SYMPTOMS, OTHER, MEDICATIONS, BIOMETRICS, CYCLE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    stepsViewModel: StepsViewModel,
    biometricsViewModel: BiometricsViewModel,
    medicationViewModel: MedicationViewModel,
    journalViewModel: JournalViewModel,
    onNavigate: (NavIntent) -> Unit
) {
    val onNavigateBack = { onNavigate(NavIntent.Back) }
    val allMeals by journalViewModel.allMeals.collectAsState()
    val allSymptoms by journalViewModel.allSymptomEntries.collectAsState()
    val allMedications by medicationViewModel.allMedications.collectAsState()
    val allOtherEntries by journalViewModel.allOtherEntries.collectAsState()
    val allBloodPressure by biometricsViewModel.allBloodPressureEntries.collectAsState()
    val allCholesterol by biometricsViewModel.allCholesterolEntries.collectAsState()
    val allWeight by biometricsViewModel.allWeightEntries.collectAsState()
    val allSpO2 by biometricsViewModel.allSpO2Entries.collectAsState()
    val allBloodGlucose by biometricsViewModel.allBloodGlucoseEntries.collectAsState()
    val allCycleEntries by journalViewModel.allCycleEntries.collectAsState()
    val showCycleTracking by journalViewModel.showCycleTracking.collectAsState()
    val allStepCountEntries by stepsViewModel.allStepCountEntries.collectAsState()
    val showStepCounting by stepsViewModel.showStepCounting.collectAsState()
    var filterType by remember { mutableStateOf(FilterType.ALL) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "History",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterType == FilterType.ALL,
                    onClick = { filterType = FilterType.ALL },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = filterType == FilterType.MEALS,
                    onClick = { filterType = FilterType.MEALS },
                    label = { Text("Meals") }
                )
                FilterChip(
                    selected = filterType == FilterType.SYMPTOMS,
                    onClick = { filterType = FilterType.SYMPTOMS },
                    label = { Text("Symptoms") }
                )
                FilterChip(
                    selected = filterType == FilterType.OTHER,
                    onClick = { filterType = FilterType.OTHER },
                    label = { Text("Other") }
                )
                FilterChip(
                    selected = filterType == FilterType.MEDICATIONS,
                    onClick = { filterType = FilterType.MEDICATIONS },
                    label = { Text("Meds") }
                )
                FilterChip(
                    selected = filterType == FilterType.BIOMETRICS,
                    onClick = { filterType = FilterType.BIOMETRICS },
                    label = { Text("Biometrics") }
                )
                if (showCycleTracking) {
                    FilterChip(
                        selected = filterType == FilterType.CYCLE,
                        onClick = { filterType = FilterType.CYCLE },
                        label = { Text("Cycle") }
                    )
                }
            }

            val filteredEntries = remember(
                filterType, allMeals, allSymptoms, allMedications, allOtherEntries,
                allBloodPressure, allCholesterol, allWeight, allSpO2, allBloodGlucose,
                allCycleEntries, allStepCountEntries, showCycleTracking, showStepCounting
            ) {
                when (filterType) {
                    FilterType.ALL -> {
                        (allMeals.map { HistoryEntry.Meal(it) } +
                                allSymptoms.map { HistoryEntry.Symptom(it) } +
                                allMedications.map { HistoryEntry.Medication(it) } +
                                allOtherEntries.map { HistoryEntry.Other(it) } +
                                allBloodPressure.map { HistoryEntry.BloodPressure(it) } +
                                allCholesterol.map { HistoryEntry.Cholesterol(it) } +
                                allWeight.map { HistoryEntry.Weight(it) } +
                                allSpO2.map { HistoryEntry.SpO2(it) } +
                                allBloodGlucose.map { HistoryEntry.BloodGlucose(it) } +
                                (if (showCycleTracking) allCycleEntries.map { HistoryEntry.Cycle(it) } else emptyList()) +
                                (if (showStepCounting) allStepCountEntries.map { HistoryEntry.StepCount(it) } else emptyList()))
                            .sortedByDescending { it.timestamp }
                    }
                    FilterType.MEALS -> allMeals.map { HistoryEntry.Meal(it) }
                        .sortedByDescending { it.timestamp }
                    FilterType.SYMPTOMS -> allSymptoms.map { HistoryEntry.Symptom(it) }
                        .sortedByDescending { it.timestamp }
                    FilterType.OTHER -> allOtherEntries.map { HistoryEntry.Other(it) }
                        .sortedByDescending { it.timestamp }
                    FilterType.MEDICATIONS -> allMedications.map { HistoryEntry.Medication(it) }
                        .sortedByDescending { it.timestamp }
                    FilterType.BIOMETRICS -> {
                        (allBloodPressure.map { HistoryEntry.BloodPressure(it) } +
                                allCholesterol.map { HistoryEntry.Cholesterol(it) } +
                                allWeight.map { HistoryEntry.Weight(it) } +
                                allSpO2.map { HistoryEntry.SpO2(it) } +
                                allBloodGlucose.map { HistoryEntry.BloodGlucose(it) } +
                                (if (showStepCounting) allStepCountEntries.map { HistoryEntry.StepCount(it) } else emptyList()))
                            .sortedByDescending { it.timestamp }
                    }
                    FilterType.CYCLE -> allCycleEntries.map { HistoryEntry.Cycle(it) }
                        .sortedByDescending { it.timestamp }
                }
            }

            if (filteredEntries.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No entries found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                val entriesByDay = remember(filteredEntries) {
                    filteredEntries.groupBy { entry ->
                        Instant.ofEpochMilli(entry.timestamp)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                }

                val today = LocalDate.now()
                val yesterday = today.minusDays(1)
                val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d")

                LazyColumn(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    entriesByDay.forEach { (date, entries) ->
                        item {
                            val dayLabel = when (date) {
                                today -> "Today"
                                yesterday -> "Yesterday"
                                else -> date.format(dateFormatter)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                                Text(
                                    text = dayLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                        items(
                            entries,
                            key = { entry -> "${entry::class.simpleName}-${entry.entryId}" }
                        ) { entry ->
                            when (entry) {
                                is HistoryEntry.Meal -> MealEntryCard(
                                    meal = entry.entry,
                                    onDelete = { journalViewModel.deleteMeal(entry.entry) },
                                    onEdit = { onNavigate(NavIntent.EditMeal(entry.entry.meal.id)) }
                                )
                                is HistoryEntry.Symptom -> SymptomEntryCard(
                                    name = entry.entry.name,
                                    severity = entry.entry.severity,
                                    notes = entry.entry.notes,
                                    startTime = entry.entry.startTime,
                                    endTime = entry.entry.endTime,
                                    onDelete = { journalViewModel.deleteSymptom(entry.entry) },
                                    onEdit = { onNavigate(NavIntent.EditSymptom(entry.entry.id)) }
                                )
                                is HistoryEntry.Other -> OtherEntryCard(
                                    entry = entry.entry,
                                    onDelete = { journalViewModel.deleteOtherEntry(entry.entry) },
                                    onEdit = { onNavigate(NavIntent.EditOther(entry.entry.id)) }
                                )
                                is HistoryEntry.Medication -> MedicationCard(
                                    entry = entry.entry,
                                    onDelete = { medicationViewModel.deleteMedication(entry.entry) },
                                    onEdit = { onNavigate(NavIntent.EditMedication(entry.entry.id)) }
                                )
                                is HistoryEntry.BloodPressure -> BloodPressureCard(
                                    entry = entry.entry,
                                    onDelete = { biometricsViewModel.deleteBloodPressure(entry.entry) },
                                    onEdit = { onNavigate(NavIntent.EditBloodPressure(entry.entry.id)) }
                                )
                                is HistoryEntry.Cholesterol -> CholesterolCard(
                                    entry = entry.entry,
                                    onDelete = { biometricsViewModel.deleteCholesterol(entry.entry) },
                                    onEdit = { onNavigate(NavIntent.EditCholesterol(entry.entry.id)) }
                                )
                                is HistoryEntry.Weight -> WeightCard(
                                    entry = entry.entry,
                                    onDelete = { biometricsViewModel.deleteWeight(entry.entry) },
                                    onEdit = { onNavigate(NavIntent.EditWeight(entry.entry.id)) }
                                )
                                is HistoryEntry.SpO2 -> SpO2Card(
                                    entry = entry.entry,
                                    onDelete = { biometricsViewModel.deleteSpO2(entry.entry) },
                                    onEdit = { onNavigate(NavIntent.EditSpO2(entry.entry.id)) }
                                )
                                is HistoryEntry.BloodGlucose -> BloodGlucoseCard(
                                    entry = entry.entry,
                                    onDelete = { biometricsViewModel.deleteBloodGlucose(entry.entry) },
                                    onEdit = { onNavigate(NavIntent.EditBloodGlucose(entry.entry.id)) }
                                )
                                is HistoryEntry.Cycle -> CycleEntryCard(
                                    entry = entry.entry,
                                    onDelete = { journalViewModel.deleteCycleEntry(entry.entry) },
                                    onEdit = { onNavigate(NavIntent.EditCycleEntry(entry.entry.id)) }
                                )
                                is HistoryEntry.StepCount -> StepCountCard(
                                    entry = entry.entry,
                                    onDelete = { stepsViewModel.deleteStepCount(entry.entry) },
                                    onEdit = { onNavigate(NavIntent.EditStepCount(entry.entry.id)) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private sealed class HistoryEntry {
    abstract val timestamp: Long
    abstract val entryId: Long

    data class Meal(val entry: MealWithDetails) : HistoryEntry() {
        override val timestamp: Long = entry.meal.timestamp
        override val entryId: Long = entry.meal.id
    }

    data class Symptom(val entry: SymptomEntry) : HistoryEntry() {
        override val timestamp: Long = entry.timestamp
        override val entryId: Long = entry.id
    }

    data class Other(val entry: OtherEntry) : HistoryEntry() {
        override val timestamp: Long = entry.timestamp
        override val entryId: Long = entry.id
    }

    data class Medication(val entry: MedicationEntry) : HistoryEntry() {
        override val timestamp: Long = entry.timestamp
        override val entryId: Long = entry.id
    }

    data class BloodPressure(val entry: BloodPressureEntry) : HistoryEntry() {
        override val timestamp: Long = entry.timestamp
        override val entryId: Long = entry.id
    }

    data class Cholesterol(val entry: CholesterolEntry) : HistoryEntry() {
        override val timestamp: Long = entry.timestamp
        override val entryId: Long = entry.id
    }

    data class Weight(val entry: WeightEntry) : HistoryEntry() {
        override val timestamp: Long = entry.timestamp
        override val entryId: Long = entry.id
    }

    data class SpO2(val entry: SpO2Entry) : HistoryEntry() {
        override val timestamp: Long = entry.timestamp
        override val entryId: Long = entry.id
    }

    data class BloodGlucose(val entry: BloodGlucoseEntry) : HistoryEntry() {
        override val timestamp: Long = entry.timestamp
        override val entryId: Long = entry.id
    }

    data class Cycle(val entry: CycleEntry) : HistoryEntry() {
        override val timestamp: Long = entry.timestamp
        override val entryId: Long = entry.id
    }

    data class StepCount(val entry: StepCountEntry) : HistoryEntry() {
        override val timestamp: Long =
            LocalDate.ofEpochDay(entry.dateEpochDay)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        override val entryId: Long = entry.id
    }
}
