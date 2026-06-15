package com.privatehealthjournal.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.privatehealthjournal.data.entity.CycleEntry
import com.privatehealthjournal.data.entity.CycleSymptom
import com.privatehealthjournal.data.entity.FlowIntensity
import com.privatehealthjournal.viewmodel.LogViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val CycleRose = Color(0xFFE91E63)

data class PeriodGroup(
    val entries: List<CycleEntry>,
    val start: LocalDate,
    val end: LocalDate
) {
    val durationDays: Int = ChronoUnit.DAYS.between(start, end).toInt() + 1
    val dominantFlow: FlowIntensity = entries
        .maxOfOrNull { it.flow.ordinal }
        ?.let { FlowIntensity.entries[it] } ?: FlowIntensity.MEDIUM
    val allSymptoms: Set<CycleSymptom> = entries
        .flatMap { CycleSymptom.decode(it.symptoms) }
        .toSet()
}

private fun groupIntoPeriods(
    entries: List<CycleEntry>,
    zone: ZoneId = ZoneId.systemDefault()
): List<PeriodGroup> {
    if (entries.isEmpty()) return emptyList()
    val sorted = entries.sortedBy { it.timestamp }
    val groups = mutableListOf<MutableList<CycleEntry>>()
    var current = mutableListOf(sorted.first())
    for (i in 1 until sorted.size) {
        val prevDate = Instant.ofEpochMilli(sorted[i - 1].timestamp).atZone(zone).toLocalDate()
        val currDate = Instant.ofEpochMilli(sorted[i].timestamp).atZone(zone).toLocalDate()
        if (ChronoUnit.DAYS.between(prevDate, currDate) <= 2) {
            current.add(sorted[i])
        } else {
            groups.add(current)
            current = mutableListOf(sorted[i])
        }
    }
    groups.add(current)
    return groups.map { group ->
        val dates = group.map { Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() }
        PeriodGroup(entries = group, start = dates.min(), end = dates.max())
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CycleTrackingScreen(
    viewModel: LogViewModel,
    onNavigateBack: () -> Unit,
    onAddEntry: () -> Unit,
    onEditEntry: (Long) -> Unit
) {
    val allCycleEntries by viewModel.allCycleEntries.collectAsState()

    val periods = remember(allCycleEntries) {
        groupIntoPeriods(allCycleEntries).sortedByDescending { it.start }
    }

    val avgCycleLength: Int? = remember(periods) {
        if (periods.size < 2) null
        else {
            val sortedByStart = periods.sortedBy { it.start }
            val gaps = sortedByStart.zipWithNext { a, b ->
                ChronoUnit.DAYS.between(a.start, b.start).toInt()
            }
            gaps.average().toInt()
        }
    }

    val avgPeriodDuration: Int? = remember(periods) {
        if (periods.isEmpty()) null
        else periods.map { it.durationDays }.average().toInt()
    }

    val predictedNext: LocalDate? = remember(periods, avgCycleLength) {
        if (periods.isEmpty() || avgCycleLength == null) null
        else periods.maxByOrNull { it.start }?.start?.plusDays(avgCycleLength.toLong())
    }

    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Cycle Tracking", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddEntry,
                containerColor = CycleRose,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Log Period")
            }
        }
    ) { paddingValues ->
        if (allCycleEntries.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = CycleRose.copy(alpha = 0.4f),
                    modifier = Modifier.padding(8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No cycle entries yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = "Tap + to log your first period day",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                // Stats card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Cycle Summary",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CycleRose
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            val lastPeriod = periods.firstOrNull()
                            if (lastPeriod != null) {
                                StatRow(
                                    label = "Last period",
                                    value = if (lastPeriod.start == lastPeriod.end) {
                                        lastPeriod.start.format(dateFormatter)
                                    } else {
                                        "${lastPeriod.start.format(dateFormatter)} – ${lastPeriod.end.format(dateFormatter)} (${lastPeriod.durationDays}d)"
                                    }
                                )
                            }
                            avgPeriodDuration?.let {
                                StatRow(label = "Avg period length", value = "$it days")
                            }
                            avgCycleLength?.let {
                                StatRow(label = "Avg cycle length", value = "$it days")
                            }
                            predictedNext?.let {
                                StatRow(label = "Next period (est.)", value = "~${it.format(dateFormatter)}")
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Period History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                items(periods) { period ->
                    PeriodCard(
                        period = period,
                        dateFormatter = dateFormatter,
                        onEditEntry = onEditEntry
                    )
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PeriodCard(
    period: PeriodGroup,
    dateFormatter: DateTimeFormatter,
    onEditEntry: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (period.start == period.end) {
                        period.start.format(dateFormatter)
                    } else {
                        "${period.start.format(dateFormatter)} – ${period.end.format(dateFormatter)}"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${period.durationDays} day${if (period.durationDays != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = period.dominantFlow.displayLabel + " flow",
                style = MaterialTheme.typography.bodyMedium,
                color = CycleRose
            )
            if (period.allSymptoms.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    period.allSymptoms.forEach { symptom ->
                        SuggestionChip(
                            onClick = {},
                            label = { Text(symptom.displayLabel, style = MaterialTheme.typography.labelSmall) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = CycleRose.copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            }
        }
    }
}
