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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import com.privatehealthjournal.data.entity.MedicationEntry
import com.privatehealthjournal.ui.components.DateTimePicker
import com.privatehealthjournal.viewmodel.LogViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddMedicationScreen(
    viewModel: LogViewModel,
    onNavigateBack: () -> Unit,
    editId: Long? = null,
    prefillName: String? = null
) {
    val editingMedication by viewModel.editingMedication.collectAsState()
    val medicationNames by viewModel.allMedicationNames.collectAsState()
    val isEditMode = editId != null

    var name by remember { mutableStateOf(prefillName ?: "") }
    var dosage by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var timestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var existingId by remember { mutableStateOf<Long?>(null) }

    // Load existing entry for editing
    LaunchedEffect(editId) {
        if (editId != null) {
            viewModel.loadMedicationForEditing(editId)
        }
    }

    // Populate fields when editing entry is loaded
    LaunchedEffect(editingMedication) {
        editingMedication?.let { entry ->
            name = entry.name
            dosage = entry.dosage
            notes = entry.notes
            timestamp = entry.timestamp
            existingId = entry.id
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Edit Medication" else "Log Medication",
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
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
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
                imageVector = Icons.Default.Medication,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Medication Name") },
                placeholder = { Text("e.g., Ibuprofen, Omeprazole") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Medication name suggestions (autocomplete)
            if (medicationNames.isNotEmpty() && name.isNotBlank()) {
                val filtered = medicationNames.filter {
                    it.contains(name, ignoreCase = true) && !it.equals(name, ignoreCase = true)
                }
                if (filtered.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Previous medications:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        filtered.take(5).forEach { suggestion ->
                            AssistChip(
                                onClick = { name = suggestion },
                                label = { Text(suggestion) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
                }
            } else if (medicationNames.isNotEmpty() && name.isBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Previous medications:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    medicationNames.take(8).forEach { suggestion ->
                        AssistChip(
                            onClick = { name = suggestion },
                            label = { Text(suggestion) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = dosage,
                onValueChange = { dosage = it },
                label = { Text("Dosage") },
                placeholder = { Text("e.g., 200mg, 1 tablet") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Date/Time Section
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
                    if (name.isNotBlank()) {
                        if (isEditMode && existingId != null) {
                            viewModel.updateMedication(
                                MedicationEntry(
                                    id = existingId!!,
                                    name = name.trim(),
                                    dosage = dosage.trim(),
                                    notes = notes.trim(),
                                    timestamp = timestamp
                                )
                            )
                        } else {
                            viewModel.addMedication(
                                name = name.trim(),
                                dosage = dosage.trim(),
                                notes = notes.trim(),
                                timestamp = timestamp
                            )
                        }
                        viewModel.clearEditingState()
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Text(if (isEditMode) "Update Medication" else "Save Medication")
            }
        }
    }
}
