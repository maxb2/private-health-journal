package com.foodsymptomlog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: LogViewModel,
    onAddMeal: () -> Unit,
    onAddSymptom: () -> Unit,
    onAddBowelMovement: () -> Unit,
    onViewHistory: () -> Unit
) {
    val recentMeals by viewModel.recentMeals.collectAsState()
    val recentSymptoms by viewModel.recentSymptomEntries.collectAsState()
    val recentBowelMovements by viewModel.recentBowelMovements.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Food Symptom Log",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onViewHistory) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History"
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
            // First row: Meal and Symptom
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onAddMeal,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("Meal")
                }

                Button(
                    onClick = onAddSymptom,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("Symptom")
                }

                Button(
                    onClick = onAddBowelMovement,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("BM")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Recent Entries",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (recentMeals.isEmpty() && recentSymptoms.isEmpty() && recentBowelMovements.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.padding(8.dp)
                    )
                    Text(
                        text = "No entries yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Start logging your meals, symptoms, and bowel movements!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            } else {
                val combinedEntries = (
                    recentMeals.map { EntryItem.Meal(it) } +
                    recentSymptoms.map { EntryItem.Symptom(it) } +
                    recentBowelMovements.map { EntryItem.BowelMovement(it) }
                )
                    .sortedByDescending {
                        when (it) {
                            is EntryItem.Meal -> it.entry.meal.timestamp
                            is EntryItem.Symptom -> it.entry.timestamp
                            is EntryItem.BowelMovement -> it.entry.timestamp
                        }
                    }
                    .take(10)

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(combinedEntries) { item ->
                        when (item) {
                            is EntryItem.Meal -> MealEntryCard(
                                meal = item.entry,
                                onDelete = { viewModel.deleteMeal(item.entry) }
                            )
                            is EntryItem.Symptom -> SymptomEntryCard(
                                name = item.entry.name,
                                severity = item.entry.severity,
                                notes = item.entry.notes,
                                startTime = item.entry.startTime,
                                endTime = item.entry.endTime,
                                onDelete = { viewModel.deleteSymptom(item.entry) }
                            )
                            is EntryItem.BowelMovement -> BowelMovementCard(
                                entry = item.entry,
                                onDelete = { viewModel.deleteBowelMovement(item.entry) }
                            )
                        }
                    }
                }
            }
        }
    }
}

private sealed class EntryItem {
    data class Meal(val entry: MealWithDetails) : EntryItem()
    data class Symptom(val entry: SymptomEntry) : EntryItem()
    data class BowelMovement(val entry: BowelMovementEntry) : EntryItem()
}
