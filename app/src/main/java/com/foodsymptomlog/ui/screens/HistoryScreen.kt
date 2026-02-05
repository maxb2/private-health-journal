package com.foodsymptomlog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.foodsymptomlog.data.entity.BowelMovementEntry
import com.foodsymptomlog.data.entity.MealWithDetails
import com.foodsymptomlog.data.entity.SymptomEntry
import com.foodsymptomlog.ui.components.BowelMovementCard
import com.foodsymptomlog.ui.components.MealEntryCard
import com.foodsymptomlog.ui.components.SymptomEntryCard
import com.foodsymptomlog.viewmodel.LogViewModel

enum class FilterType {
    ALL, MEALS, SYMPTOMS, BOWEL_MOVEMENTS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: LogViewModel,
    onNavigateBack: () -> Unit,
    onEditMeal: (Long) -> Unit = {},
    onEditSymptom: (Long) -> Unit = {},
    onEditBowelMovement: (Long) -> Unit = {}
) {
    val allMeals by viewModel.allMeals.collectAsState()
    val allSymptoms by viewModel.allSymptomEntries.collectAsState()
    val allBowelMovements by viewModel.allBowelMovements.collectAsState()
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
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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
                modifier = Modifier.fillMaxWidth(),
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
                    selected = filterType == FilterType.BOWEL_MOVEMENTS,
                    onClick = { filterType = FilterType.BOWEL_MOVEMENTS },
                    label = { Text("BM") }
                )
            }

            val filteredEntries = when (filterType) {
                FilterType.ALL -> {
                    (allMeals.map { HistoryEntry.Meal(it) } +
                            allSymptoms.map { HistoryEntry.Symptom(it) } +
                            allBowelMovements.map { HistoryEntry.BowelMovement(it) })
                        .sortedByDescending { it.timestamp }
                }
                FilterType.MEALS -> allMeals.map { HistoryEntry.Meal(it) }
                    .sortedByDescending { it.timestamp }
                FilterType.SYMPTOMS -> allSymptoms.map { HistoryEntry.Symptom(it) }
                    .sortedByDescending { it.timestamp }
                FilterType.BOWEL_MOVEMENTS -> allBowelMovements.map { HistoryEntry.BowelMovement(it) }
                    .sortedByDescending { it.timestamp }
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
                LazyColumn(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredEntries) { entry ->
                        when (entry) {
                            is HistoryEntry.Meal -> MealEntryCard(
                                meal = entry.entry,
                                onDelete = { viewModel.deleteMeal(entry.entry) },
                                onEdit = { onEditMeal(entry.entry.meal.id) }
                            )
                            is HistoryEntry.Symptom -> SymptomEntryCard(
                                name = entry.entry.name,
                                severity = entry.entry.severity,
                                notes = entry.entry.notes,
                                startTime = entry.entry.startTime,
                                endTime = entry.entry.endTime,
                                onDelete = { viewModel.deleteSymptom(entry.entry) },
                                onEdit = { onEditSymptom(entry.entry.id) }
                            )
                            is HistoryEntry.BowelMovement -> BowelMovementCard(
                                entry = entry.entry,
                                onDelete = { viewModel.deleteBowelMovement(entry.entry) },
                                onEdit = { onEditBowelMovement(entry.entry.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

private sealed class HistoryEntry {
    abstract val timestamp: Long

    data class Meal(val entry: MealWithDetails) : HistoryEntry() {
        override val timestamp: Long = entry.meal.timestamp
    }

    data class Symptom(val entry: SymptomEntry) : HistoryEntry() {
        override val timestamp: Long = entry.timestamp
    }

    data class BowelMovement(val entry: BowelMovementEntry) : HistoryEntry() {
        override val timestamp: Long = entry.timestamp
    }
}
