package com.privatehealthjournal.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import com.privatehealthjournal.R
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
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    stepsViewModel: StepsViewModel,
    biometricsViewModel: BiometricsViewModel,
    medicationViewModel: MedicationViewModel,
    journalViewModel: JournalViewModel,
    onNavigate: (NavIntent) -> Unit
) {
    val recentMeals by journalViewModel.recentMeals.collectAsState()
    val recentSymptoms by journalViewModel.recentSymptomEntries.collectAsState()
    val recentMedications by medicationViewModel.recentMedications.collectAsState()
    val recentOtherEntries by journalViewModel.recentOtherEntries.collectAsState()
    val recentBloodPressure by biometricsViewModel.recentBloodPressureEntries.collectAsState()
    val recentCholesterol by biometricsViewModel.recentCholesterolEntries.collectAsState()
    val recentWeight by biometricsViewModel.recentWeightEntries.collectAsState()
    val recentSpO2 by biometricsViewModel.recentSpO2Entries.collectAsState()
    val recentBloodGlucose by biometricsViewModel.recentBloodGlucoseEntries.collectAsState()
    val recentCycleEntries by journalViewModel.recentCycleEntries.collectAsState()
    val showCycleTracking by journalViewModel.showCycleTracking.collectAsState()
    val recentStepCount by stepsViewModel.recentStepCountEntries.collectAsState()
    val showStepCounting by stepsViewModel.showStepCounting.collectAsState()

    var medsMenuExpanded by remember { mutableStateOf(false) }
    var biometricsMenuExpanded by remember { mutableStateOf(false) }
    var otherMenuExpanded by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val drawerItems = listOf(
        Triple("Meal Budget", Icons.Default.Savings, NavIntent.MealBudget),
        Triple("Calendar", Icons.Default.CalendarMonth, NavIntent.Calendar),
        Triple("View Charts", Icons.Default.ShowChart, NavIntent.BiometricsChart),
        Triple("History", Icons.Default.History, NavIntent.History),
        Triple("Settings", Icons.Default.Settings, NavIntent.Settings),
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                ModalDrawerSheet {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Private Health Journal",
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    drawerItems.forEach { (label, icon, intent) ->
                        NavigationDrawerItem(
                            icon = { Icon(icon, contentDescription = null) },
                            label = { Text(label) },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                onNavigate(intent)
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }
        }
    ) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Private Health Journal",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    Image(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = "App logo",
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(48.dp)
                    )
                },
                actions = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open menu"
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
            // Action buttons - flow to fit screen width
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Meal button
                Button(
                    onClick = { onNavigate(NavIntent.AddMeal) },
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
                    onClick = { onNavigate(NavIntent.AddSymptom()) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
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

                // Medication dropdown
                Box {
                    Button(
                        onClick = { medsMenuExpanded = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Medication,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Meds")
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = medsMenuExpanded,
                        onDismissRequest = { medsMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Log Medication") },
                            onClick = {
                                medsMenuExpanded = false
                                onNavigate(NavIntent.AddMedication())
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Medication Sets") },
                            onClick = {
                                medsMenuExpanded = false
                                onNavigate(NavIntent.MedicationSets)
                            }
                        )
                    }
                }

                // Biometrics dropdown
                Box {
                    Button(
                        onClick = { biometricsMenuExpanded = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = com.privatehealthjournal.ui.theme.biometricAccentColor()
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
                                onNavigate(NavIntent.AddBloodPressure)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Cholesterol") },
                            onClick = {
                                biometricsMenuExpanded = false
                                onNavigate(NavIntent.AddCholesterol)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Weight") },
                            onClick = {
                                biometricsMenuExpanded = false
                                onNavigate(NavIntent.AddWeight)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("SpO2 (Blood Oxygen)") },
                            onClick = {
                                biometricsMenuExpanded = false
                                onNavigate(NavIntent.AddSpO2)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Blood Glucose") },
                            onClick = {
                                biometricsMenuExpanded = false
                                onNavigate(NavIntent.AddBloodGlucose)
                            }
                        )
                        if (showStepCounting) {
                            DropdownMenuItem(
                                text = { Text("Steps") },
                                onClick = {
                                    biometricsMenuExpanded = false
                                    onNavigate(NavIntent.AddStepCount)
                                }
                            )
                        }
                    }
                }

                // Other dropdown
                Box {
                    Button(
                        onClick = { otherMenuExpanded = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = com.privatehealthjournal.ui.theme.otherAccentColor()
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
                                onNavigate(NavIntent.AddOther("BOWEL_MOVEMENT"))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sleep") },
                            onClick = {
                                otherMenuExpanded = false
                                onNavigate(NavIntent.AddOther("SLEEP"))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Exercise") },
                            onClick = {
                                otherMenuExpanded = false
                                onNavigate(NavIntent.AddOther("EXERCISE"))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Stress") },
                            onClick = {
                                otherMenuExpanded = false
                                onNavigate(NavIntent.AddOther("STRESS"))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Mood") },
                            onClick = {
                                otherMenuExpanded = false
                                onNavigate(NavIntent.AddOther("MOOD"))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Water Intake") },
                            onClick = {
                                otherMenuExpanded = false
                                onNavigate(NavIntent.AddOther("WATER_INTAKE"))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Other") },
                            onClick = {
                                otherMenuExpanded = false
                                onNavigate(NavIntent.AddOther("OTHER"))
                            }
                        )
                    }
                }

                // Cycle tracking button (hidden when showCycleTracking is false)
                if (showCycleTracking) {
                    Button(
                        onClick = { onNavigate(NavIntent.CycleTracking) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = com.privatehealthjournal.ui.theme.cycleAccentColor()
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cycle")
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
                recentWeight.isEmpty() && recentSpO2.isEmpty() &&
                recentBloodGlucose.isEmpty() &&
                (!showCycleTracking || recentCycleEntries.isEmpty()) &&
                (!showStepCounting || recentStepCount.isEmpty())

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
                val entriesByDay = remember(
                    recentMeals, recentSymptoms, recentMedications, recentOtherEntries,
                    recentBloodPressure, recentCholesterol, recentWeight, recentSpO2,
                    recentBloodGlucose, recentCycleEntries, recentStepCount,
                    showCycleTracking, showStepCounting
                ) {
                    val combined = (
                        recentMeals.map { EntryItem.Meal(it) } +
                        recentSymptoms.map { EntryItem.Symptom(it) } +
                        recentMedications.map { EntryItem.Medication(it) } +
                        recentOtherEntries.map { EntryItem.Other(it) } +
                        recentBloodPressure.map { EntryItem.BloodPressure(it) } +
                        recentCholesterol.map { EntryItem.Cholesterol(it) } +
                        recentWeight.map { EntryItem.Weight(it) } +
                        recentSpO2.map { EntryItem.SpO2(it) } +
                        recentBloodGlucose.map { EntryItem.BloodGlucose(it) } +
                        (if (showCycleTracking) recentCycleEntries.map { EntryItem.Cycle(it) } else emptyList()) +
                        (if (showStepCounting) recentStepCount.map { EntryItem.StepCount(it) } else emptyList())
                    )
                        .sortedByDescending { it.timestamp }
                        .take(10)
                    combined.groupBy { item ->
                        Instant.ofEpochMilli(item.timestamp)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
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
                            key = { item -> "${item::class.simpleName}-${item.entryId}" }
                        ) { item ->
                            when (item) {
                                is EntryItem.Meal -> MealEntryCard(
                                    meal = item.entry,
                                    onDelete = { journalViewModel.deleteMeal(item.entry) },
                                    onEdit = { onNavigate(NavIntent.EditMeal(item.entry.meal.id)) }
                                )
                                is EntryItem.Symptom -> SymptomEntryCard(
                                    name = item.entry.name,
                                    severity = item.entry.severity,
                                    notes = item.entry.notes,
                                    startTime = item.entry.startTime,
                                    endTime = item.entry.endTime,
                                    onDelete = { journalViewModel.deleteSymptom(item.entry) },
                                    onEdit = { onNavigate(NavIntent.EditSymptom(item.entry.id)) }
                                )
                                is EntryItem.Other -> OtherEntryCard(
                                    entry = item.entry,
                                    onDelete = { journalViewModel.deleteOtherEntry(item.entry) },
                                    onEdit = { onNavigate(NavIntent.EditOther(item.entry.id)) }
                                )
                                is EntryItem.Medication -> MedicationCard(
                                    entry = item.entry,
                                    onDelete = { medicationViewModel.deleteMedication(item.entry) },
                                    onEdit = { onNavigate(NavIntent.EditMedication(item.entry.id)) }
                                )
                                is EntryItem.BloodPressure -> BloodPressureCard(
                                    entry = item.entry,
                                    onDelete = { biometricsViewModel.deleteBloodPressure(item.entry) },
                                    onEdit = { onNavigate(NavIntent.EditBloodPressure(item.entry.id)) }
                                )
                                is EntryItem.Cholesterol -> CholesterolCard(
                                    entry = item.entry,
                                    onDelete = { biometricsViewModel.deleteCholesterol(item.entry) },
                                    onEdit = { onNavigate(NavIntent.EditCholesterol(item.entry.id)) }
                                )
                                is EntryItem.Weight -> WeightCard(
                                    entry = item.entry,
                                    onDelete = { biometricsViewModel.deleteWeight(item.entry) },
                                    onEdit = { onNavigate(NavIntent.EditWeight(item.entry.id)) }
                                )
                                is EntryItem.SpO2 -> SpO2Card(
                                    entry = item.entry,
                                    onDelete = { biometricsViewModel.deleteSpO2(item.entry) },
                                    onEdit = { onNavigate(NavIntent.EditSpO2(item.entry.id)) }
                                )
                                is EntryItem.BloodGlucose -> BloodGlucoseCard(
                                    entry = item.entry,
                                    onDelete = { biometricsViewModel.deleteBloodGlucose(item.entry) },
                                    onEdit = { onNavigate(NavIntent.EditBloodGlucose(item.entry.id)) }
                                )
                                is EntryItem.Cycle -> CycleEntryCard(
                                    entry = item.entry,
                                    onDelete = { journalViewModel.deleteCycleEntry(item.entry) },
                                    onEdit = { onNavigate(NavIntent.EditCycleEntry(item.entry.id)) }
                                )
                                is EntryItem.StepCount -> StepCountCard(
                                    entry = item.entry,
                                    onDelete = { stepsViewModel.deleteStepCount(item.entry) },
                                    onEdit = { onNavigate(NavIntent.EditStepCount(item.entry.id)) }
                                )
                            }
                        }
                    }
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
    abstract val entryId: Long

    data class Meal(val entry: MealWithDetails) : EntryItem() {
        override val timestamp: Long = entry.meal.timestamp
        override val entryId: Long = entry.meal.id
    }
    data class Symptom(val entry: SymptomEntry) : EntryItem() {
        override val timestamp: Long = entry.timestamp
        override val entryId: Long = entry.id
    }
    data class Medication(val entry: MedicationEntry) : EntryItem() {
        override val timestamp: Long = entry.timestamp
        override val entryId: Long = entry.id
    }
    data class Other(val entry: OtherEntry) : EntryItem() {
        override val timestamp: Long = entry.timestamp
        override val entryId: Long = entry.id
    }
    data class BloodPressure(val entry: BloodPressureEntry) : EntryItem() {
        override val timestamp: Long = entry.timestamp
        override val entryId: Long = entry.id
    }
    data class Cholesterol(val entry: CholesterolEntry) : EntryItem() {
        override val timestamp: Long = entry.timestamp
        override val entryId: Long = entry.id
    }
    data class Weight(val entry: WeightEntry) : EntryItem() {
        override val timestamp: Long = entry.timestamp
        override val entryId: Long = entry.id
    }
    data class SpO2(val entry: SpO2Entry) : EntryItem() {
        override val timestamp: Long = entry.timestamp
        override val entryId: Long = entry.id
    }
    data class BloodGlucose(val entry: BloodGlucoseEntry) : EntryItem() {
        override val timestamp: Long = entry.timestamp
        override val entryId: Long = entry.id
    }
    data class Cycle(val entry: CycleEntry) : EntryItem() {
        override val timestamp: Long = entry.timestamp
        override val entryId: Long = entry.id
    }
    data class StepCount(val entry: StepCountEntry) : EntryItem() {
        override val timestamp: Long =
            LocalDate.ofEpochDay(entry.dateEpochDay)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        override val entryId: Long = entry.id
    }
}
