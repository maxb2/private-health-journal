package com.privatehealthjournal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
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
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
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

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val zone = ZoneId.systemDefault()

    // Group entries by date
    val entriesByDate = remember(allMeals, allSymptoms, allMedications, allOtherEntries, allBloodPressure, allCholesterol, allWeight, allSpO2, allBloodGlucose, allCycleEntries, showCycleTracking, allStepCountEntries, showStepCounting) {
        val map = mutableMapOf<LocalDate, MutableList<CalendarEntry>>()

        allMeals.forEach { meal ->
            val date = Instant.ofEpochMilli(meal.meal.timestamp).atZone(zone).toLocalDate()
            map.getOrPut(date) { mutableListOf() }.add(CalendarEntry.Meal(meal))
        }
        allSymptoms.forEach { symptom ->
            val date = Instant.ofEpochMilli(symptom.timestamp).atZone(zone).toLocalDate()
            map.getOrPut(date) { mutableListOf() }.add(CalendarEntry.Symptom(symptom))
        }
        allMedications.forEach { med ->
            val date = Instant.ofEpochMilli(med.timestamp).atZone(zone).toLocalDate()
            map.getOrPut(date) { mutableListOf() }.add(CalendarEntry.Medication(med))
        }
        allOtherEntries.forEach { other ->
            val date = Instant.ofEpochMilli(other.timestamp).atZone(zone).toLocalDate()
            map.getOrPut(date) { mutableListOf() }.add(CalendarEntry.Other(other))
        }
        allBloodPressure.forEach { bp ->
            val date = Instant.ofEpochMilli(bp.timestamp).atZone(zone).toLocalDate()
            map.getOrPut(date) { mutableListOf() }.add(CalendarEntry.BloodPressure(bp))
        }
        allCholesterol.forEach { chol ->
            val date = Instant.ofEpochMilli(chol.timestamp).atZone(zone).toLocalDate()
            map.getOrPut(date) { mutableListOf() }.add(CalendarEntry.Cholesterol(chol))
        }
        allWeight.forEach { weight ->
            val date = Instant.ofEpochMilli(weight.timestamp).atZone(zone).toLocalDate()
            map.getOrPut(date) { mutableListOf() }.add(CalendarEntry.Weight(weight))
        }
        allSpO2.forEach { spo2 ->
            val date = Instant.ofEpochMilli(spo2.timestamp).atZone(zone).toLocalDate()
            map.getOrPut(date) { mutableListOf() }.add(CalendarEntry.SpO2(spo2))
        }
        allBloodGlucose.forEach { bg ->
            val date = Instant.ofEpochMilli(bg.timestamp).atZone(zone).toLocalDate()
            map.getOrPut(date) { mutableListOf() }.add(CalendarEntry.BloodGlucose(bg))
        }
        if (showCycleTracking) {
            allCycleEntries.forEach { cycle ->
                val date = Instant.ofEpochMilli(cycle.timestamp).atZone(zone).toLocalDate()
                map.getOrPut(date) { mutableListOf() }.add(CalendarEntry.Cycle(cycle))
            }
        }
        if (showStepCounting) {
            allStepCountEntries.forEach { step ->
                val date = LocalDate.ofEpochDay(step.dateEpochDay)
                map.getOrPut(date) { mutableListOf() }.add(CalendarEntry.StepCount(step))
            }
        }

        map
    }

    val cycleDates = remember(allCycleEntries, showCycleTracking) {
        if (!showCycleTracking) emptySet()
        else allCycleEntries.map { Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() }.toSet()
    }

    val selectedEntries = entriesByDate[selectedDate]
        ?.sortedByDescending {
            when (it) {
                is CalendarEntry.Meal -> it.entry.meal.timestamp
                is CalendarEntry.Symptom -> it.entry.timestamp
                is CalendarEntry.Medication -> it.entry.timestamp
                is CalendarEntry.Other -> it.entry.timestamp
                is CalendarEntry.BloodPressure -> it.entry.timestamp
                is CalendarEntry.Cholesterol -> it.entry.timestamp
                is CalendarEntry.Weight -> it.entry.timestamp
                is CalendarEntry.SpO2 -> it.entry.timestamp
                is CalendarEntry.BloodGlucose -> it.entry.timestamp
                is CalendarEntry.Cycle -> it.entry.timestamp
                is CalendarEntry.StepCount -> it.entry.timestamp
            }
        }
        ?: emptyList()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Calendar",
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
            // Month navigation
            MonthNavigationHeader(
                currentMonth = currentMonth,
                onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                onNextMonth = { currentMonth = currentMonth.plusMonths(1) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Day of week headers
            DayOfWeekHeaders()

            Spacer(modifier = Modifier.height(4.dp))

            // Calendar grid
            CalendarGrid(
                yearMonth = currentMonth,
                selectedDate = selectedDate,
                datesWithEntries = entriesByDate.keys,
                cycleDates = cycleDates,
                onDateSelected = { selectedDate = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Selected date entries
            Text(
                text = "${selectedDate.dayOfMonth} ${selectedDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${selectedDate.year}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (selectedEntries.isEmpty()) {
                Text(
                    text = "No entries for this day",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        selectedEntries,
                        key = { entry -> "${entry::class.simpleName}-${entry.entryId}" }
                    ) { entry ->
                        when (entry) {
                            is CalendarEntry.Meal -> MealEntryCard(
                                meal = entry.entry,
                                onDelete = { journalViewModel.deleteMeal(entry.entry) },
                                onEdit = { onNavigate(NavIntent.EditMeal(entry.entry.meal.id)) }
                            )
                            is CalendarEntry.Symptom -> SymptomEntryCard(
                                name = entry.entry.name,
                                severity = entry.entry.severity,
                                notes = entry.entry.notes,
                                startTime = entry.entry.startTime,
                                endTime = entry.entry.endTime,
                                onDelete = { journalViewModel.deleteSymptom(entry.entry) },
                                onEdit = { onNavigate(NavIntent.EditSymptom(entry.entry.id)) }
                            )
                            is CalendarEntry.Medication -> MedicationCard(
                                entry = entry.entry,
                                onDelete = { medicationViewModel.deleteMedication(entry.entry) },
                                onEdit = { onNavigate(NavIntent.EditMedication(entry.entry.id)) }
                            )
                            is CalendarEntry.Other -> OtherEntryCard(
                                entry = entry.entry,
                                onDelete = { journalViewModel.deleteOtherEntry(entry.entry) },
                                onEdit = { onNavigate(NavIntent.EditOther(entry.entry.id)) }
                            )
                            is CalendarEntry.BloodPressure -> BloodPressureCard(
                                entry = entry.entry,
                                onDelete = { biometricsViewModel.deleteBloodPressure(entry.entry) },
                                onEdit = { onNavigate(NavIntent.EditBloodPressure(entry.entry.id)) }
                            )
                            is CalendarEntry.Cholesterol -> CholesterolCard(
                                entry = entry.entry,
                                onDelete = { biometricsViewModel.deleteCholesterol(entry.entry) },
                                onEdit = { onNavigate(NavIntent.EditCholesterol(entry.entry.id)) }
                            )
                            is CalendarEntry.Weight -> WeightCard(
                                entry = entry.entry,
                                onDelete = { biometricsViewModel.deleteWeight(entry.entry) },
                                onEdit = { onNavigate(NavIntent.EditWeight(entry.entry.id)) }
                            )
                            is CalendarEntry.SpO2 -> SpO2Card(
                                entry = entry.entry,
                                onDelete = { biometricsViewModel.deleteSpO2(entry.entry) },
                                onEdit = { onNavigate(NavIntent.EditSpO2(entry.entry.id)) }
                            )
                            is CalendarEntry.BloodGlucose -> BloodGlucoseCard(
                                entry = entry.entry,
                                onDelete = { biometricsViewModel.deleteBloodGlucose(entry.entry) },
                                onEdit = { onNavigate(NavIntent.EditBloodGlucose(entry.entry.id)) }
                            )
                            is CalendarEntry.Cycle -> CycleEntryCard(
                                entry = entry.entry,
                                onDelete = { journalViewModel.deleteCycleEntry(entry.entry) },
                                onEdit = { onNavigate(NavIntent.EditCycleEntry(entry.entry.id)) }
                            )
                            is CalendarEntry.StepCount -> StepCountCard(
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

@Composable
private fun MonthNavigationHeader(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Previous month"
            )
        }
        Text(
            text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Next month"
            )
        }
    }
}

@Composable
private fun DayOfWeekHeaders() {
    val daysOfWeek = listOf(
        DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY
    )
    Row(modifier = Modifier.fillMaxWidth()) {
        daysOfWeek.forEach { day ->
            Text(
                text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    datesWithEntries: Set<LocalDate>,
    cycleDates: Set<LocalDate> = emptySet(),
    onDateSelected: (LocalDate) -> Unit
) {
    val firstOfMonth = yearMonth.atDay(1)
    // Sunday = 0, Monday = 1, ..., Saturday = 6
    val startOffset = firstOfMonth.dayOfWeek.value % 7
    val daysInMonth = yearMonth.lengthOfMonth()
    val totalCells = startOffset + daysInMonth
    val rows = (totalCells + 6) / 7

    Column {
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0..6) {
                    val dayIndex = row * 7 + col - startOffset + 1
                    if (dayIndex in 1..daysInMonth) {
                        val date = yearMonth.atDay(dayIndex)
                        val isSelected = date == selectedDate
                        val hasEntries = date in datesWithEntries
                        val hasCycleEntry = date in cycleDates
                        val isToday = date == LocalDate.now()

                        CalendarDayCell(
                            day = dayIndex,
                            isSelected = isSelected,
                            isToday = isToday,
                            hasEntries = hasEntries,
                            hasCycleEntry = hasCycleEntry,
                            onClick = { onDateSelected(date) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    hasEntries: Boolean,
    hasCycleEntry: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surface
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
            )
            if (hasEntries) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.primary
                        )
                )
            }
            if (hasCycleEntry) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else Color(0xFFE91E63)
                        )
                )
            }
        }
    }
}

private sealed class CalendarEntry {
    abstract val entryId: Long

    data class Meal(val entry: MealWithDetails) : CalendarEntry() {
        override val entryId: Long = entry.meal.id
    }
    data class Symptom(val entry: SymptomEntry) : CalendarEntry() {
        override val entryId: Long = entry.id
    }
    data class Medication(val entry: MedicationEntry) : CalendarEntry() {
        override val entryId: Long = entry.id
    }
    data class Other(val entry: OtherEntry) : CalendarEntry() {
        override val entryId: Long = entry.id
    }
    data class BloodPressure(val entry: BloodPressureEntry) : CalendarEntry() {
        override val entryId: Long = entry.id
    }
    data class Cholesterol(val entry: CholesterolEntry) : CalendarEntry() {
        override val entryId: Long = entry.id
    }
    data class Weight(val entry: WeightEntry) : CalendarEntry() {
        override val entryId: Long = entry.id
    }
    data class SpO2(val entry: SpO2Entry) : CalendarEntry() {
        override val entryId: Long = entry.id
    }
    data class BloodGlucose(val entry: BloodGlucoseEntry) : CalendarEntry() {
        override val entryId: Long = entry.id
    }
    data class Cycle(val entry: CycleEntry) : CalendarEntry() {
        override val entryId: Long = entry.id
    }
    data class StepCount(val entry: StepCountEntry) : CalendarEntry() {
        override val entryId: Long = entry.id
    }
}
