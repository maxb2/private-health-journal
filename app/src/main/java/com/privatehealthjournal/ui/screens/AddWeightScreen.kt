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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.privatehealthjournal.data.entity.WeightEntry
import com.privatehealthjournal.data.entity.WeightUnit
import com.privatehealthjournal.ui.components.DateTimePicker
import com.privatehealthjournal.viewmodel.LogViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddWeightScreen(
    viewModel: LogViewModel,
    onNavigateBack: () -> Unit,
    editId: Long? = null
) {
    val editingEntry by viewModel.editingWeight.collectAsState()
    val isEditMode = editId != null

    var weight by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf(WeightUnit.LB) }
    var notes by remember { mutableStateOf("") }
    var timestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var existingId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(editId) {
        if (editId != null) {
            viewModel.loadWeightForEditing(editId)
        }
    }

    LaunchedEffect(editingEntry) {
        editingEntry?.let { entry ->
            weight = entry.weight.toString()
            unit = entry.unit
            notes = entry.notes
            timestamp = entry.timestamp
            existingId = entry.id
        }
    }

    val isValid = weight.toDoubleOrNull() != null && weight.toDoubleOrNull()!! > 0

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Edit Weight" else "Log Weight",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearEditingState()
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
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
            Icon(
                imageVector = Icons.Default.MonitorWeight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Weight",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = weight,
                onValueChange = { input ->
                    // Allow digits and one decimal point
                    val filtered = input.filter { it.isDigit() || it == '.' }
                    if (filtered.count { it == '.' } <= 1) {
                        weight = filtered
                    }
                },
                label = { Text("Weight") },
                placeholder = { Text(if (unit == WeightUnit.LB) "150" else "68") },
                suffix = { Text(if (unit == WeightUnit.LB) "lbs" else "kg") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Unit",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WeightUnit.entries.forEach { weightUnit ->
                    FilterChip(
                        selected = unit == weightUnit,
                        onClick = { unit = weightUnit },
                        label = {
                            Text(
                                when (weightUnit) {
                                    WeightUnit.LB -> "Pounds (lbs)"
                                    WeightUnit.KG -> "Kilograms (kg)"
                                }
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            DateTimePicker(
                timestamp = timestamp,
                onTimestampChange = { timestamp = it },
                label = "Date & Time"
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                placeholder = { Text("Any additional details...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val weightValue = weight.toDoubleOrNull() ?: return@Button

                    if (isEditMode && existingId != null) {
                        viewModel.updateWeight(
                            WeightEntry(
                                id = existingId!!,
                                weight = weightValue,
                                unit = unit,
                                notes = notes.trim(),
                                timestamp = timestamp
                            )
                        )
                    } else {
                        viewModel.addWeight(
                            weight = weightValue,
                            unit = unit,
                            notes = notes.trim(),
                            timestamp = timestamp
                        )
                    }
                    viewModel.clearEditingState()
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isValid
            ) {
                Text(if (isEditMode) "Update Weight" else "Save Weight")
            }
        }
    }
}
