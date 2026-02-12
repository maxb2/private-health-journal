package com.privatehealthjournal.ui.screens

import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Air
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.privatehealthjournal.data.entity.SpO2Entry
import com.privatehealthjournal.ui.components.DateTimePicker
import com.privatehealthjournal.viewmodel.LogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSpO2Screen(
    viewModel: LogViewModel,
    onNavigateBack: () -> Unit,
    editId: Long? = null
) {
    val editingEntry by viewModel.editingSpO2.collectAsState()
    val isEditMode = editId != null

    var spo2 by remember { mutableStateOf("") }
    var pulse by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var timestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var existingId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(editId) {
        if (editId != null) {
            viewModel.loadSpO2ForEditing(editId)
        }
    }

    LaunchedEffect(editingEntry) {
        editingEntry?.let { entry ->
            spo2 = entry.spo2.toString()
            pulse = entry.pulse?.toString() ?: ""
            notes = entry.notes
            timestamp = entry.timestamp
            existingId = entry.id
        }
    }

    val spo2Value = spo2.toIntOrNull()
    val isValid = spo2Value != null && spo2Value in 0..100

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Edit SpO2" else "Log SpO2",
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
                imageVector = Icons.Default.Air,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Blood Oxygen Saturation",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = spo2,
                onValueChange = { spo2 = it.filter { c -> c.isDigit() }.take(3) },
                label = { Text("SpO2") },
                placeholder = { Text("98") },
                suffix = { Text("%") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = spo2Value != null && spo2Value !in 0..100,
                supportingText = if (spo2Value != null && spo2Value !in 0..100) {
                    { Text("Must be between 0 and 100") }
                } else null
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = pulse,
                onValueChange = { pulse = it.filter { c -> c.isDigit() } },
                label = { Text("Pulse (optional)") },
                placeholder = { Text("72") },
                suffix = { Text("bpm") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

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
                    val spo2Val = spo2.toIntOrNull() ?: return@Button
                    val pulseVal = pulse.toIntOrNull()

                    if (isEditMode && existingId != null) {
                        viewModel.updateSpO2(
                            SpO2Entry(
                                id = existingId!!,
                                spo2 = spo2Val,
                                pulse = pulseVal,
                                notes = notes.trim(),
                                timestamp = timestamp
                            )
                        )
                    } else {
                        viewModel.addSpO2(
                            spo2 = spo2Val,
                            pulse = pulseVal,
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
                Text(if (isEditMode) "Update SpO2" else "Save SpO2")
            }
        }
    }
}
