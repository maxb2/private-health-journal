package com.foodsymptomlog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.foodsymptomlog.data.entity.BloodPressureEntry
import com.foodsymptomlog.data.entity.CholesterolEntry
import com.foodsymptomlog.data.entity.MealWithDetails
import com.foodsymptomlog.data.entity.MedicationEntry
import com.foodsymptomlog.data.entity.OtherEntry
import com.foodsymptomlog.data.entity.SymptomEntry
import com.foodsymptomlog.data.entity.WeightEntry
import com.foodsymptomlog.ui.components.BloodPressureCard
import com.foodsymptomlog.ui.components.CholesterolCard
import com.foodsymptomlog.ui.components.MealEntryCard
import com.foodsymptomlog.ui.components.MedicationCard
import com.foodsymptomlog.ui.components.OtherEntryCard
import com.foodsymptomlog.ui.components.SymptomEntryCard
import com.foodsymptomlog.ui.components.WeightCard
import com.foodsymptomlog.viewmodel.LogViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: LogViewModel,
    onAddMeal: () -> Unit,
    onAddSymptom: (String?) -> Unit,
    onAddOther: (String) -> Unit,
    onAddMedication: (String?) -> Unit,
    onAddBloodPressure: () -> Unit,
    onAddCholesterol: () -> Unit,
    onAddWeight: () -> Unit,
    onViewBiometricsChart: () -> Unit = {},
    onViewHistory: () -> Unit,
    onViewCalendar: () -> Unit = {},
    onViewSettings: () -> Unit = {},
    onEditMeal: (Long) -> Unit = {},
    onEditSymptom: (Long) -> Unit = {},
    onEditOther: (Long) -> Unit = {},
    onEditMedication: (Long) -> Unit = {},
    onEditBloodPressure: (Long) -> Unit = {},
    onEditCholesterol: (Long) -> Unit = {},
    onEditWeight: (Long) -> Unit = {}
) {
    val recentMeals by viewModel.recentMeals.collectAsState()
    val recentSymptoms by viewModel.recentSymptomEntries.collectAsState()
    val recentMedications by viewModel.recentMedications.collectAsState()
    val recentOtherEntries by viewModel.recentOtherEntries.collectAsState()
    val recentBloodPressure by viewModel.recentBloodPressureEntries.collectAsState()
    val recentCholesterol by viewModel.recentCholesterolEntries.collectAsState()
    val recentWeight by viewModel.recentWeightEntries.collectAsState()

    var biometricsMenuExpanded by remember { mutableStateOf(false) }
    var otherMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Food Symptom Log",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onViewCalendar) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Calendar"
                        )
                    }
                    IconButton(onClick = onViewHistory) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History"
                        )
                    }
                    IconButton(onClick = onViewSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
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
            // First row: Meal, Symptom, Medication
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Meal button
                Button(
                    onClick = { onAddMeal() },
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
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Meal")
                }

                // Symptom button
                Button(
                    onClick = { onAddSymptom(null) },
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
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Symptom")
                }

                // Medication button
                Button(
                    onClick = { onAddMedication(null) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Medication,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Meds")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Second row: Biometrics and Other
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Biometrics dropdown
                Box(modifier = Modifier.weight(1f)) {
                    Button(
                        onClick = { biometricsMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonitorHeart,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Biometrics")
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = biometricsMenuExpanded,
                        onDismissRequest = { biometricsMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Blood Pressure") },
                            onClick = {
                                biometricsMenuExpanded = false
                                onAddBloodPressure()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Cholesterol") },
                            onClick = {
                                biometricsMenuExpanded = false
                                onAddCholesterol()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Weight") },
                            onClick = {
                                biometricsMenuExpanded = false
                                onAddWeight()
                            }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("View Charts") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ShowChart,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                biometricsMenuExpanded = false
                                onViewBiometricsChart()
                            }
                        )
                    }
                }

                // Other dropdown
                Box(modifier = Modifier.weight(1f)) {
                    Button(
                        onClick = { otherMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Other")
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = otherMenuExpanded,
                        onDismissRequest = { otherMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Bowel Movement") },
                            onClick = {
                                otherMenuExpanded = false
                                onAddOther("BOWEL_MOVEMENT")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sleep") },
                            onClick = {
                                otherMenuExpanded = false
                                onAddOther("SLEEP")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Exercise") },
                            onClick = {
                                otherMenuExpanded = false
                                onAddOther("EXERCISE")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Stress") },
                            onClick = {
                                otherMenuExpanded = false
                                onAddOther("STRESS")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Water Intake") },
                            onClick = {
                                otherMenuExpanded = false
                                onAddOther("WATER_INTAKE")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Other") },
                            onClick = {
                                otherMenuExpanded = false
                                onAddOther("OTHER")
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Recent Entries",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            val allEmpty = recentMeals.isEmpty() && recentSymptoms.isEmpty() &&
                recentMedications.isEmpty() && recentOtherEntries.isEmpty() &&
                recentBloodPressure.isEmpty() && recentCholesterol.isEmpty() &&
                recentWeight.isEmpty()

            if (allEmpty) {
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
                        text = "Start logging your meals, symptoms, and more!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            } else {
                val combinedEntries = (
                    recentMeals.map { EntryItem.Meal(it) } +
                    recentSymptoms.map { EntryItem.Symptom(it) } +
                    recentMedications.map { EntryItem.Medication(it) } +
                    recentOtherEntries.map { EntryItem.Other(it) } +
                    recentBloodPressure.map { EntryItem.BloodPressure(it) } +
                    recentCholesterol.map { EntryItem.Cholesterol(it) } +
                    recentWeight.map { EntryItem.Weight(it) }
                )
                    .sortedByDescending { it.timestamp }
                    .take(10)

                // Group entries by day
                val entriesByDay = combinedEntries.groupBy { item ->
                    Instant.ofEpochMilli(item.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
                }

                val today = LocalDate.now()
                val yesterday = today.minusDays(1)
                val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d")

                LazyColumn(
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
                                Divider(
                                    modifier = Modifier.weight(1f),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                                Text(
                                    text = dayLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                                Divider(
                                    modifier = Modifier.weight(1f),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                        items(entries) { item ->
                            when (item) {
                                is EntryItem.Meal -> MealEntryCard(
                                    meal = item.entry,
                                    onDelete = { viewModel.deleteMeal(item.entry) },
                                    onEdit = { onEditMeal(item.entry.meal.id) }
                                )
                                is EntryItem.Symptom -> SymptomEntryCard(
                                    name = item.entry.name,
                                    severity = item.entry.severity,
                                    notes = item.entry.notes,
                                    startTime = item.entry.startTime,
                                    endTime = item.entry.endTime,
                                    onDelete = { viewModel.deleteSymptom(item.entry) },
                                    onEdit = { onEditSymptom(item.entry.id) }
                                )
                                is EntryItem.Other -> OtherEntryCard(
                                    entry = item.entry,
                                    onDelete = { viewModel.deleteOtherEntry(item.entry) },
                                    onEdit = { onEditOther(item.entry.id) }
                                )
                                is EntryItem.Medication -> MedicationCard(
                                    entry = item.entry,
                                    onDelete = { viewModel.deleteMedication(item.entry) },
                                    onEdit = { onEditMedication(item.entry.id) }
                                )
                                is EntryItem.BloodPressure -> BloodPressureCard(
                                    entry = item.entry,
                                    onDelete = { viewModel.deleteBloodPressure(item.entry) },
                                    onEdit = { onEditBloodPressure(item.entry.id) }
                                )
                                is EntryItem.Cholesterol -> CholesterolCard(
                                    entry = item.entry,
                                    onDelete = { viewModel.deleteCholesterol(item.entry) },
                                    onEdit = { onEditCholesterol(item.entry.id) }
                                )
                                is EntryItem.Weight -> WeightCard(
                                    entry = item.entry,
                                    onDelete = { viewModel.deleteWeight(item.entry) },
                                    onEdit = { onEditWeight(item.entry.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private sealed class EntryItem {
    abstract val timestamp: Long

    data class Meal(val entry: MealWithDetails) : EntryItem() {
        override val timestamp: Long = entry.meal.timestamp
    }
    data class Symptom(val entry: SymptomEntry) : EntryItem() {
        override val timestamp: Long = entry.timestamp
    }
    data class Medication(val entry: MedicationEntry) : EntryItem() {
        override val timestamp: Long = entry.timestamp
    }
    data class Other(val entry: OtherEntry) : EntryItem() {
        override val timestamp: Long = entry.timestamp
    }
    data class BloodPressure(val entry: BloodPressureEntry) : EntryItem() {
        override val timestamp: Long = entry.timestamp
    }
    data class Cholesterol(val entry: CholesterolEntry) : EntryItem() {
        override val timestamp: Long = entry.timestamp
    }
    data class Weight(val entry: WeightEntry) : EntryItem() {
        override val timestamp: Long = entry.timestamp
    }
}
