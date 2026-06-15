package com.privatehealthjournal.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.privatehealthjournal.data.entity.MealType
import com.privatehealthjournal.data.entity.MealWithDetails
import com.privatehealthjournal.data.entity.OtherEntry
import com.privatehealthjournal.data.entity.OtherEntryType
import com.privatehealthjournal.data.entity.StepCountEntry
import com.privatehealthjournal.viewmodel.LogViewModel
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealBudgetScreen(
    viewModel: LogViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val allMeals by viewModel.allMeals.collectAsState()
    val allOtherEntries by viewModel.allOtherEntries.collectAsState()
    val allStepCountEntries by viewModel.allStepCountEntries.collectAsState()
    val stepsPerPointCredit by viewModel.stepsPerPointCredit.collectAsState()
    val dailyBudgetState by viewModel.dailyBudget.collectAsState()
    val dailyBudget = dailyBudgetState

    fun stepCreditsForDay(day: LocalDate): Int {
        val divisor = stepsPerPointCredit ?: return 0
        if (divisor <= 0) return 0
        val entry = allStepCountEntries.firstOrNull { it.dateEpochDay == day.toEpochDay() } ?: return 0
        return entry.steps / divisor
    }

    var weekOffset by remember { mutableIntStateOf(0) }
    var selectedDay by remember { mutableStateOf<LocalDate?>(null) }

    val today = LocalDate.now()
    val weekStart = today.plusWeeks(weekOffset.toLong())
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val weekEnd = weekStart.plusDays(6)

    val weekStartMs = weekStart.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val weekEndMs = weekEnd.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    val zone = ZoneId.systemDefault()

    val weekMeals = allMeals.filter { it.meal.timestamp in weekStartMs until weekEndMs }
    val weekExercise = allOtherEntries.filter {
        it.entryType == OtherEntryType.EXERCISE && it.timestamp in weekStartMs until weekEndMs
    }

    val weeklyMealPoints = weekMeals.sumOf { it.meal.pointCost ?: 0 }
    val weeklyExerciseCredits = weekExercise.sumOf { it.pointCredit ?: 0 }
    val weeklyStepCredits = (0..6).sumOf { stepCreditsForDay(weekStart.plusDays(it.toLong())) }
    val netWeeklyPoints = weeklyMealPoints - weeklyExerciseCredits - weeklyStepCredits

    val weeklyBudget = (dailyBudget ?: 0) * 8
    val weeklyRemaining = weeklyBudget - netWeeklyPoints

    // Sum of each day's overage (how much was drawn from the overage pool)
    val overageUsed = (0..6).sumOf { dayOffset ->
        val day = weekStart.plusDays(dayOffset.toLong())
        if (day.isAfter(today)) return@sumOf 0
        val dayStartMs = day.atStartOfDay(zone).toInstant().toEpochMilli()
        val dayEndMs = day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val dayMealPts = allMeals
            .filter { it.meal.timestamp in dayStartMs until dayEndMs }
            .sumOf { it.meal.pointCost ?: 0 }
        val dayExercisePts = allOtherEntries
            .filter { it.entryType == OtherEntryType.EXERCISE && it.timestamp in dayStartMs until dayEndMs }
            .sumOf { it.pointCredit ?: 0 }
        val dayStepPts = stepCreditsForDay(day)
        maxOf(0, dayMealPts - dayExercisePts - dayStepPts - (dailyBudget ?: 0))
    }

    val headerFormatter = DateTimeFormatter.ofPattern("MMM d")
    val dayFormatter = DateTimeFormatter.ofPattern("EEE\nMMM d")
    val isCurrentWeek = weekOffset == 0

    // Reset selected day when week changes
    val weekKey = weekOffset
    remember(weekKey) { selectedDay = null }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Meal Budget", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
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
                .verticalScroll(rememberScrollState())
        ) {
            if (dailyBudget == null) {
                NoBudgetCard(onNavigateToSettings)
            } else {

            // Week navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = {
                    weekOffset--
                    selectedDay = null
                }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous week")
                }
                Text(
                    text = "${weekStart.format(headerFormatter)} – ${weekEnd.format(headerFormatter)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                IconButton(
                    onClick = {
                        weekOffset++
                        selectedDay = null
                    },
                    enabled = !isCurrentWeek
                ) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Next week",
                        tint = if (!isCurrentWeek) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Weekly summary card
            WeeklySummaryCard(
                dailyBudget = dailyBudget,
                weeklyBudget = weeklyBudget,
                netWeeklyPoints = netWeeklyPoints,
                weeklyMealPoints = weeklyMealPoints,
                weeklyExerciseCredits = weeklyExerciseCredits,
                weeklyStepCredits = weeklyStepCredits,
                weeklyRemaining = weeklyRemaining,
                overageUsed = overageUsed
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Daily breakdown
            Text(
                text = "Daily Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            (0..6).forEach { dayOffset ->
                val day = weekStart.plusDays(dayOffset.toLong())
                val dayStartMs = day.atStartOfDay(zone).toInstant().toEpochMilli()
                val dayEndMs = day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
                val isFuture = day.isAfter(today)

                val dayMeals = allMeals.filter { it.meal.timestamp in dayStartMs until dayEndMs }
                val dayExercise = allOtherEntries.filter {
                    it.entryType == OtherEntryType.EXERCISE && it.timestamp in dayStartMs until dayEndMs
                }
                val dayMealPoints = dayMeals.sumOf { it.meal.pointCost ?: 0 }
                val dayExerciseCredits = dayExercise.sumOf { it.pointCredit ?: 0 }
                val dayStepEntry = allStepCountEntries.firstOrNull { it.dateEpochDay == day.toEpochDay() }
                val dayStepCredits = stepCreditsForDay(day)
                val dayNet = dayMealPoints - dayExerciseCredits - dayStepCredits
                val isExpanded = selectedDay == day

                DayRow(
                    day = day,
                    dayNet = dayNet,
                    dailyBudget = dailyBudget,
                    isFuture = isFuture,
                    isToday = day == today,
                    isExpanded = isExpanded,
                    dayMeals = dayMeals,
                    dayExercise = dayExercise,
                    dayStepEntry = dayStepEntry,
                    dayStepCredits = dayStepCredits,
                    dayFormatter = dayFormatter,
                    onClick = {
                        selectedDay = if (isExpanded) null else day
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            } // end else (budget set)
        }
    }
}

@Composable
private fun NoBudgetCard(onNavigateToSettings: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No budget set",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Set your daily point budget in Settings to start tracking.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            androidx.compose.material3.Button(onClick = onNavigateToSettings) {
                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Settings")
            }
        }
    }
}

@Composable
private fun WeeklySummaryCard(
    dailyBudget: Int,
    weeklyBudget: Int,
    netWeeklyPoints: Int,
    weeklyMealPoints: Int,
    weeklyExerciseCredits: Int,
    weeklyStepCredits: Int,
    weeklyRemaining: Int,
    overageUsed: Int
) {
    val overageBudget = dailyBudget
    val progress = if (weeklyBudget > 0) (netWeeklyPoints.toFloat() / weeklyBudget).coerceIn(0f, 1f) else 0f
    val isOver = netWeeklyPoints > weeklyBudget

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isOver -> MaterialTheme.colorScheme.errorContainer
                netWeeklyPoints > dailyBudget * 7 -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.primaryContainer
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "This Week",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "$netWeeklyPoints pts",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "/ $weeklyBudget pts",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = if (isOver) MaterialTheme.colorScheme.error
                else if (netWeeklyPoints > dailyBudget * 7) MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$dailyBudget pts/day × 7 days + $dailyBudget overage = $weeklyBudget total",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            if (weeklyExerciseCredits > 0 || weeklyStepCredits > 0) {
                val parts = buildList {
                    add("Meal pts: $weeklyMealPoints")
                    if (weeklyExerciseCredits > 0) add("Exercise credits: −$weeklyExerciseCredits")
                    if (weeklyStepCredits > 0) add("Step credits: −$weeklyStepCredits")
                }
                Text(
                    text = parts.joinToString("  |  "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = when {
                    isOver -> "${-weeklyRemaining} pts over weekly budget"
                    overageUsed > 0 -> "$overageUsed / $overageBudget overage pts used  •  $weeklyRemaining pts remaining"
                    else -> "$weeklyRemaining pts remaining  •  $overageBudget overage pts available"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (isOver) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DayRow(
    day: LocalDate,
    dayNet: Int,
    dailyBudget: Int,
    isFuture: Boolean,
    isToday: Boolean,
    isExpanded: Boolean,
    dayMeals: List<MealWithDetails>,
    dayExercise: List<OtherEntry>,
    dayStepEntry: StepCountEntry?,
    dayStepCredits: Int,
    dayFormatter: DateTimeFormatter,
    onClick: () -> Unit
) {
    val progress = if (dailyBudget > 0) (dayNet.toFloat() / dailyBudget).coerceIn(0f, 1f) else 0f
    val isOver = dayNet > dailyBudget
    val hasItems = dayMeals.isNotEmpty() || dayExercise.isNotEmpty() || dayStepCredits > 0
    val barColor = when {
        isFuture -> Color.Transparent
        isOver -> MaterialTheme.colorScheme.error
        dayNet > dailyBudget * 0.8f -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isFuture && hasItems, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isToday -> MaterialTheme.colorScheme.secondaryContainer
                isFuture -> MaterialTheme.colorScheme.surface
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column {
            // Summary row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(48.dp)
                ) {
                    Text(
                        text = day.format(dayFormatter),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (isToday) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    if (!isFuture) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth(),
                            color = barColor,
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                    }
                }
                Text(
                    text = if (isFuture) "—" else "$dayNet pts",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = when {
                        isFuture -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        isOver -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.width(56.dp),
                    textAlign = TextAlign.End
                )
                if (!isFuture && hasItems) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Expanded items
            if (isExpanded) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    dayMeals.forEach { mealWithDetails ->
                        val meal = mealWithDetails.meal
                        val foods = mealWithDetails.foods.joinToString(", ") { it.name }
                        val mealTime = Instant.ofEpochMilli(meal.timestamp)
                            .atZone(ZoneId.systemDefault())
                            .toLocalTime()
                            .format(timeFormatter)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Restaurant,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${mealTypeName(meal.mealType)}  ·  $mealTime",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                if (foods.isNotEmpty()) {
                                    Text(
                                        text = foods,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            Text(
                                text = if (meal.pointCost != null) "${meal.pointCost} pts" else "—",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = if (meal.pointCost != null) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }

                    if (dayStepEntry != null && dayStepCredits > 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.DirectionsRun,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Steps",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${dayStepEntry.steps} steps",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            Text(
                                text = "−$dayStepCredits pts",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }

                    dayExercise.forEach { exercise ->
                        val exerciseTime = Instant.ofEpochMilli(exercise.timestamp)
                            .atZone(ZoneId.systemDefault())
                            .toLocalTime()
                            .format(timeFormatter)
                        val label = buildString {
                            if (exercise.subType.isNotBlank()) append(exercise.subType)
                            if (exercise.value.isNotBlank()) {
                                if (isNotEmpty()) append("  ·  ")
                                append(exercise.value)
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.DirectionsRun,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Exercise  ·  $exerciseTime",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                if (label.isNotEmpty()) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            Text(
                                text = if (exercise.pointCredit != null) "−${exercise.pointCredit} pts" else "—",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = if (exercise.pointCredit != null) Color(0xFF4CAF50)
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun mealTypeName(type: MealType) = when (type) {
    MealType.BREAKFAST -> "Breakfast"
    MealType.LUNCH -> "Lunch"
    MealType.DINNER -> "Dinner"
    MealType.SNACK -> "Snack"
}
