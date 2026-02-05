package com.foodsymptomlog.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.foodsymptomlog.data.entity.SymptomEntry
import com.foodsymptomlog.ui.components.DateTimePicker
import com.foodsymptomlog.viewmodel.LogViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSymptomScreen(
    viewModel: LogViewModel,
    onNavigateBack: () -> Unit,
    editId: Long? = null
) {
    val editingSymptom by viewModel.editingSymptom.collectAsState()
    val isEditMode = editId != null

    var symptomName by remember { mutableStateOf("") }
    var severity by remember { mutableFloatStateOf(3f) }
    var notes by remember { mutableStateOf("") }
    var existingId by remember { mutableStateOf<Long?>(null) }

    val currentTime = System.currentTimeMillis()
    var startTime by remember { mutableLongStateOf(currentTime) }
    var hasEndTime by remember { mutableStateOf(false) }
    var endTime by remember { mutableLongStateOf(currentTime) }

    // Load existing entry for editing
    LaunchedEffect(editId) {
        if (editId != null) {
            viewModel.loadSymptomForEditing(editId)
        }
    }

    // Populate fields when editing entry is loaded
    LaunchedEffect(editingSymptom) {
        editingSymptom?.let { entry ->
            symptomName = entry.name
            severity = entry.severity.toFloat()
            notes = entry.notes
            startTime = entry.startTime
            hasEndTime = entry.endTime != null
            endTime = entry.endTime ?: currentTime
            existingId = entry.id
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Edit Symptom" else "Log Symptom",
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
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
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
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = symptomName,
                onValueChange = { symptomName = it },
                label = { Text("Symptom") },
                placeholder = { Text("What symptom are you experiencing?") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Severity: ${severity.roundToInt()}/5",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = severity,
                onValueChange = { severity = it },
                valueRange = 1f..5f,
                steps = 3,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.secondary,
                    activeTrackColor = MaterialTheme.colorScheme.secondary
                )
            )

            Text(
                text = getSeverityDescription(severity.roundToInt()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Start Time Section
            DateTimePicker(
                timestamp = startTime,
                onTimestampChange = { startTime = it },
                label = "Start Time"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // End Time Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { hasEndTime = !hasEndTime }
            ) {
                Checkbox(
                    checked = hasEndTime,
                    onCheckedChange = { hasEndTime = it }
                )
                Text(
                    text = "Symptom has ended",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (hasEndTime) {
                Spacer(modifier = Modifier.height(8.dp))

                DateTimePicker(
                    timestamp = endTime,
                    onTimestampChange = { endTime = it },
                    label = "End Time"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                placeholder = { Text("Any additional details...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (symptomName.isNotBlank()) {
                        if (isEditMode && existingId != null) {
                            viewModel.updateSymptom(
                                SymptomEntry(
                                    id = existingId!!,
                                    name = symptomName.trim(),
                                    severity = severity.roundToInt(),
                                    startTime = startTime,
                                    endTime = if (hasEndTime) endTime else null,
                                    notes = notes.trim()
                                )
                            )
                        } else {
                            viewModel.addSymptom(
                                name = symptomName.trim(),
                                severity = severity.roundToInt(),
                                startTime = startTime,
                                endTime = if (hasEndTime) endTime else null,
                                notes = notes.trim()
                            )
                        }
                        viewModel.clearEditingState()
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = symptomName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text(if (isEditMode) "Update Symptom" else "Save Symptom Entry")
            }
        }
    }
}

private fun getSeverityDescription(severity: Int): String {
    return when (severity) {
        1 -> "Mild - Barely noticeable"
        2 -> "Minor - Slightly bothersome"
        3 -> "Moderate - Noticeable discomfort"
        4 -> "Severe - Significant discomfort"
        5 -> "Very Severe - Intense discomfort"
        else -> ""
    }
}
