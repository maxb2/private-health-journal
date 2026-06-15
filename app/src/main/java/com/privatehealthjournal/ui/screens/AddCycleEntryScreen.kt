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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.privatehealthjournal.data.entity.CycleEntry
import com.privatehealthjournal.data.entity.CycleSymptom
import com.privatehealthjournal.data.entity.FlowIntensity
import com.privatehealthjournal.ui.components.DateTimePicker
import com.privatehealthjournal.ui.components.EntryTopAppBar
import com.privatehealthjournal.ui.components.rememberEditingEntry
import com.privatehealthjournal.viewmodel.LogViewModel

private val CycleRose = Color(0xFFE91E63)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddCycleEntryScreen(
    viewModel: LogViewModel,
    onNavigateBack: () -> Unit,
    editId: Long? = null
) {
    val isEditMode = editId != null

    var selectedFlow by rememberSaveable { mutableStateOf(FlowIntensity.MEDIUM) }
    var selectedSymptoms by rememberSaveable { mutableStateOf(emptySet<CycleSymptom>()) }
    var notes by rememberSaveable { mutableStateOf("") }
    var timestamp by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
    var existingId by rememberSaveable { mutableStateOf<Long?>(null) }

    rememberEditingEntry(
        editId = editId,
        editingFlow = viewModel.editingCycleEntry,
        load = { viewModel.loadCycleEntryForEditing(it) }
    ) { entry ->
        selectedFlow = entry.flow
        selectedSymptoms = CycleSymptom.decode(entry.symptoms)
        notes = entry.notes
        timestamp = entry.timestamp
        existingId = entry.id
    }

    val handleBack = {
        viewModel.clearEditingState()
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            EntryTopAppBar(
                title = if (isEditMode) "Edit Period Entry" else "Log Period",
                onBack = handleBack
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
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = CycleRose,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Flow Intensity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FlowIntensity.entries.forEach { intensity ->
                    FilterChip(
                        selected = selectedFlow == intensity,
                        onClick = { selectedFlow = intensity },
                        label = { Text(intensity.displayLabel) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CycleRose,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Symptoms",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CycleSymptom.entries.forEach { symptom ->
                    FilterChip(
                        selected = symptom in selectedSymptoms,
                        onClick = {
                            selectedSymptoms = if (symptom in selectedSymptoms) {
                                selectedSymptoms - symptom
                            } else {
                                selectedSymptoms + symptom
                            }
                        },
                        label = { Text(symptom.displayLabel) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CycleRose.copy(alpha = 0.2f),
                            selectedLabelColor = CycleRose
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
                    val entry = CycleEntry(
                        id = existingId ?: 0,
                        flow = selectedFlow,
                        symptoms = CycleSymptom.encode(selectedSymptoms),
                        notes = notes.trim(),
                        timestamp = timestamp
                    )
                    if (isEditMode && existingId != null) {
                        viewModel.updateCycleEntry(entry)
                    } else {
                        viewModel.addCycleEntry(entry)
                    }
                    viewModel.clearEditingState()
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditMode) "Update Entry" else "Save Entry")
            }
        }
    }
}
