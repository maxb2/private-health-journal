package com.privatehealthjournal.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.privatehealthjournal.data.entity.BloodGlucoseEntry
import com.privatehealthjournal.data.entity.GlucoseMealContext
import com.privatehealthjournal.data.entity.GlucoseUnit
import com.privatehealthjournal.ui.components.DateTimePicker
import com.privatehealthjournal.viewmodel.LogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBloodGlucoseScreen(
    viewModel: LogViewModel,
    onNavigateBack: () -> Unit,
    editId: Long? = null
) {
    val editingEntry by viewModel.editingBloodGlucose.collectAsState()
    val isEditMode = editId != null

    var glucoseLevel by rememberSaveable { mutableStateOf("") }
    var unit by rememberSaveable { mutableStateOf(GlucoseUnit.MG_DL) }
    var mealContext by rememberSaveable { mutableStateOf<GlucoseMealContext?>(null) }
    var notes by rememberSaveable { mutableStateOf("") }
    var timestamp by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
    var existingId by rememberSaveable { mutableStateOf<Long?>(null) }

    LaunchedEffect(editId) {
        if (editId != null) {
            viewModel.loadBloodGlucoseForEditing(editId)
        }
    }

    LaunchedEffect(editingEntry) {
        editingEntry?.let { entry ->
            glucoseLevel = if (entry.glucoseLevel == entry.glucoseLevel.toLong().toDouble()) {
                entry.glucoseLevel.toLong().toString()
            } else {
                entry.glucoseLevel.toString()
            }
            unit = entry.unit
            mealContext = entry.mealContext
            notes = entry.notes
            timestamp = entry.timestamp
            existingId = entry.id
        }
    }

    val glucoseValue = glucoseLevel.toDoubleOrNull()
    val isValid = glucoseValue != null && glucoseValue > 0

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Edit Blood Glucose" else "Log Blood Glucose",
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
                imageVector = Icons.Default.Bloodtype,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Blood Glucose Level",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = glucoseLevel,
                onValueChange = { glucoseLevel = it.filter { c -> c.isDigit() || c == '.' }.take(6) },
                label = { Text("Glucose Level") },
                placeholder = { Text(if (unit == GlucoseUnit.MG_DL) "100" else "5.6") },
                suffix = { Text(unit.displayLabel) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = glucoseValue != null && glucoseValue <= 0,
                supportingText = if (glucoseValue != null && glucoseValue <= 0) {
                    { Text("Must be a positive number") }
                } else null
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Unit selector
            Text(
                text = "Unit",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                GlucoseUnit.entries.forEach { glucoseUnit ->
                    FilterChip(
                        selected = unit == glucoseUnit,
                        onClick = { unit = glucoseUnit },
                        label = { Text(glucoseUnit.displayLabel) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Meal context selector
            Text(
                text = "Meal Context (optional)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                FilterChip(
                    selected = mealContext == null,
                    onClick = { mealContext = null },
                    label = { Text("None") }
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                GlucoseMealContext.entries.forEach { context ->
                    FilterChip(
                        selected = mealContext == context,
                        onClick = { mealContext = context },
                        label = { Text(context.displayLabel) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
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
                    val level = glucoseLevel.toDoubleOrNull() ?: return@Button

                    if (isEditMode && existingId != null) {
                        viewModel.updateBloodGlucose(
                            BloodGlucoseEntry(
                                id = existingId!!,
                                glucoseLevel = level,
                                unit = unit,
                                mealContext = mealContext,
                                notes = notes.trim(),
                                timestamp = timestamp
                            )
                        )
                    } else {
                        viewModel.addBloodGlucose(
                            glucoseLevel = level,
                            unit = unit,
                            mealContext = mealContext,
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
                Text(if (isEditMode) "Update Blood Glucose" else "Save Blood Glucose")
            }
        }
    }
}
