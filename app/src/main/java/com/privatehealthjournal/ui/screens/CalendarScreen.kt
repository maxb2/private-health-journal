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
import androidx.compose.material.icons.filled.ArrowBack
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
import com.privatehealthjournal.data.entity.BloodPressureEntry
import com.privatehealthjournal.data.entity.CholesterolEntry
import com.privatehealthjournal.data.entity.MealWithDetails
import com.privatehealthjournal.data.entity.MedicationEntry
import com.privatehealthjournal.data.entity.OtherEntry
import com.privatehealthjournal.data.entity.SymptomEntry
import com.privatehealthjournal.data.entity.WeightEntry
import com.privatehealthjournal.ui.components.BloodPressureCard
import com.privatehealthjournal.ui.components.CholesterolCard
import com.privatehealthjournal.ui.components.MealEntryCard
import com.privatehealthjournal.ui.components.MedicationCard
import com.privatehealthjournal.ui.components.OtherEntryCard
import com.privatehealthjournal.ui.components.SymptomEntryCard
import com.privatehealthjournal.ui.components.WeightCard
import com.privatehealthjournal.viewmodel.LogViewModel
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
    viewModel: LogViewModel,
    onNavigateBack: () -> Unit,
    onEditMeal: (Long) -> Unit = {},
    onEditSymptom: (Long) -> Unit = {},
    onEditOther: (Long) -> Unit = {},
    onEditMedication: (Long) -> Unit = {},
    onEditBloodPressure: (Long) -> Unit = {},
    onEditCholesterol: (Long) -> Unit = {},
    onEditWeight: (Long) -> Unit = {}
) {
    val allMeals by viewModel.allMeals.collectAsState()
    val allSymptoms by viewModel.allSymptomEntries.collectAsState()
    val allMedications by viewModel.allMedications.collectAsState()
    val allOtherEntries by viewModel.allOtherEntries.collectAsState()
    val allBloodPressure by viewModel.allBloodPressureEntries.collectAsState()
    val allCholesterol by viewModel.allCholesterolEntries.collectAsState()
    val allWeight by viewModel.allWeightEntries.collectAsState()

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val zone = ZoneId.systemDefault()

    // Group entries by date
    val entriesByDate = remember(allMeals, allSymptoms, allMedications, allOtherEntries, allBloodPressure, allCholesterol, allWeight) {
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

        map
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
                    items(selectedEntries) { entry ->
                        when (entry) {
                            is CalendarEntry.Meal -> MealEntryCard(
                                meal = entry.entry,
                                onDelete = { viewModel.deleteMeal(entry.entry) },
                                onEdit = { onEditMeal(entry.entry.meal.id) }
                            )
                            is CalendarEntry.Symptom -> SymptomEntryCard(
                                name = entry.entry.name,
                                severity = entry.entry.severity,
                                notes = entry.entry.notes,
                                startTime = entry.entry.startTime,
                                endTime = entry.entry.endTime,
                                onDelete = { viewModel.deleteSymptom(entry.entry) },
                                onEdit = { onEditSymptom(entry.entry.id) }
                            )
                            is CalendarEntry.Medication -> MedicationCard(
                                entry = entry.entry,
                                onDelete = { viewModel.deleteMedication(entry.entry) },
                                onEdit = { onEditMedication(entry.entry.id) }
                            )
                            is CalendarEntry.Other -> OtherEntryCard(
                                entry = entry.entry,
                                onDelete = { viewModel.deleteOtherEntry(entry.entry) },
                                onEdit = { onEditOther(entry.entry.id) }
                            )
                            is CalendarEntry.BloodPressure -> BloodPressureCard(
                                entry = entry.entry,
                                onDelete = { viewModel.deleteBloodPressure(entry.entry) },
                                onEdit = { onEditBloodPressure(entry.entry.id) }
                            )
                            is CalendarEntry.Cholesterol -> CholesterolCard(
                                entry = entry.entry,
                                onDelete = { viewModel.deleteCholesterol(entry.entry) },
                                onEdit = { onEditCholesterol(entry.entry.id) }
                            )
                            is CalendarEntry.Weight -> WeightCard(
                                entry = entry.entry,
                                onDelete = { viewModel.deleteWeight(entry.entry) },
                                onEdit = { onEditWeight(entry.entry.id) }
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
                        val isToday = date == LocalDate.now()

                        CalendarDayCell(
                            day = dayIndex,
                            isSelected = isSelected,
                            isToday = isToday,
                            hasEntries = hasEntries,
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
        }
    }
}

private sealed class CalendarEntry {
    data class Meal(val entry: MealWithDetails) : CalendarEntry()
    data class Symptom(val entry: SymptomEntry) : CalendarEntry()
    data class Medication(val entry: MedicationEntry) : CalendarEntry()
    data class Other(val entry: OtherEntry) : CalendarEntry()
    data class BloodPressure(val entry: BloodPressureEntry) : CalendarEntry()
    data class Cholesterol(val entry: CholesterolEntry) : CalendarEntry()
    data class Weight(val entry: WeightEntry) : CalendarEntry()
}
