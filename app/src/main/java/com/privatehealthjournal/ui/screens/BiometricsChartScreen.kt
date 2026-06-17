package com.privatehealthjournal.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.privatehealthjournal.ui.components.BloodPressureChart
import com.privatehealthjournal.ui.components.BloodPressureSummaryCard
import com.privatehealthjournal.ui.components.CholesterolChart
import com.privatehealthjournal.ui.components.CholesterolSummaryCard
import com.privatehealthjournal.ui.components.BloodGlucoseChart
import com.privatehealthjournal.ui.components.BloodGlucoseSummaryCard
import com.privatehealthjournal.ui.components.SpO2Chart
import com.privatehealthjournal.ui.components.SpO2SummaryCard
import com.privatehealthjournal.ui.components.StepCountChart
import com.privatehealthjournal.ui.components.StepCountSummaryCard
import com.privatehealthjournal.ui.components.WeightChart
import com.privatehealthjournal.ui.components.WeightSummaryCard
import com.privatehealthjournal.ui.utils.TimeRange
import com.privatehealthjournal.ui.utils.filterByTimeRange
import com.privatehealthjournal.viewmodel.BiometricsViewModel
import com.privatehealthjournal.viewmodel.StepsViewModel

enum class BiometricTab(val title: String) {
    WEIGHT("Weight"),
    BLOOD_PRESSURE("Blood Pressure"),
    CHOLESTEROL("Cholesterol"),
    SPO2("SpO2"),
    BLOOD_GLUCOSE("Blood Glucose"),
    STEPS("Steps")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BiometricsChartScreen(
    stepsViewModel: StepsViewModel,
    biometricsViewModel: BiometricsViewModel,
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedTimeRange by remember { mutableStateOf(TimeRange.THIRTY_DAYS) }

    val allWeightEntries by biometricsViewModel.allWeightEntries.collectAsState()
    val allBloodPressureEntries by biometricsViewModel.allBloodPressureEntries.collectAsState()
    val allCholesterolEntries by biometricsViewModel.allCholesterolEntries.collectAsState()
    val allSpO2Entries by biometricsViewModel.allSpO2Entries.collectAsState()
    val allBloodGlucoseEntries by biometricsViewModel.allBloodGlucoseEntries.collectAsState()
    val allStepCountEntries by stepsViewModel.allStepCountEntries.collectAsState()
    val showStepCounting by stepsViewModel.showStepCounting.collectAsState()

    val filteredWeightEntries = remember(allWeightEntries, selectedTimeRange) {
        filterByTimeRange(allWeightEntries, selectedTimeRange) { it.timestamp }
    }

    val filteredBloodPressureEntries = remember(allBloodPressureEntries, selectedTimeRange) {
        filterByTimeRange(allBloodPressureEntries, selectedTimeRange) { it.timestamp }
    }

    val filteredCholesterolEntries = remember(allCholesterolEntries, selectedTimeRange) {
        filterByTimeRange(allCholesterolEntries, selectedTimeRange) { it.timestamp }
    }

    val filteredSpO2Entries = remember(allSpO2Entries, selectedTimeRange) {
        filterByTimeRange(allSpO2Entries, selectedTimeRange) { it.timestamp }
    }

    val filteredBloodGlucoseEntries = remember(allBloodGlucoseEntries, selectedTimeRange) {
        filterByTimeRange(allBloodGlucoseEntries, selectedTimeRange) { it.timestamp }
    }

    val filteredStepCountEntries = remember(allStepCountEntries, selectedTimeRange) {
        filterByTimeRange(allStepCountEntries, selectedTimeRange) { it.timestamp }
    }

    val visibleTabs = remember(showStepCounting) {
        if (showStepCounting) BiometricTab.entries
        else BiometricTab.entries.filter { it != BiometricTab.STEPS }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Biometrics Charts",
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
        ) {
            // Tab Row
            PrimaryScrollableTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                visibleTabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(tab.title) }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Time Range Filter Chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimeRange.entries.forEach { range ->
                        FilterChip(
                            selected = selectedTimeRange == range,
                            onClick = { selectedTimeRange = range },
                            label = { Text(range.label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Chart Content
                val activeTab = visibleTabs.getOrElse(selectedTab) { visibleTabs.first() }
                when (activeTab) {
                    BiometricTab.WEIGHT -> {
                        WeightChart(entries = filteredWeightEntries)
                        Spacer(modifier = Modifier.height(16.dp))
                        WeightSummaryCard(entries = filteredWeightEntries)
                    }
                    BiometricTab.BLOOD_PRESSURE -> {
                        BloodPressureChart(entries = filteredBloodPressureEntries)
                        Spacer(modifier = Modifier.height(16.dp))
                        BloodPressureSummaryCard(entries = filteredBloodPressureEntries)
                    }
                    BiometricTab.CHOLESTEROL -> {
                        CholesterolChart(entries = filteredCholesterolEntries)
                        Spacer(modifier = Modifier.height(16.dp))
                        CholesterolSummaryCard(entries = filteredCholesterolEntries)
                    }
                    BiometricTab.SPO2 -> {
                        SpO2Chart(entries = filteredSpO2Entries)
                        Spacer(modifier = Modifier.height(16.dp))
                        SpO2SummaryCard(entries = filteredSpO2Entries)
                    }
                    BiometricTab.BLOOD_GLUCOSE -> {
                        BloodGlucoseChart(entries = filteredBloodGlucoseEntries)
                        Spacer(modifier = Modifier.height(16.dp))
                        BloodGlucoseSummaryCard(entries = filteredBloodGlucoseEntries)
                    }
                    BiometricTab.STEPS -> {
                        StepCountChart(entries = filteredStepCountEntries)
                        Spacer(modifier = Modifier.height(16.dp))
                        StepCountSummaryCard(entries = filteredStepCountEntries)
                    }
                }
            }
        }
    }
}
