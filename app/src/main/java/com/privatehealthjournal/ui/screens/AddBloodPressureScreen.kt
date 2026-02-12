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
import androidx.compose.material.icons.filled.Favorite
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
import com.privatehealthjournal.data.entity.BloodPressureEntry
import com.privatehealthjournal.ui.components.DateTimePicker
import com.privatehealthjournal.viewmodel.LogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBloodPressureScreen(
    viewModel: LogViewModel,
    onNavigateBack: () -> Unit,
    editId: Long? = null
) {
    val editingEntry by viewModel.editingBloodPressure.collectAsState()
    val isEditMode = editId != null

    var systolic by remember { mutableStateOf("") }
    var diastolic by remember { mutableStateOf("") }
    var pulse by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var timestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var existingId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(editId) {
        if (editId != null) {
            viewModel.loadBloodPressureForEditing(editId)
        }
    }

    LaunchedEffect(editingEntry) {
        editingEntry?.let { entry ->
            systolic = entry.systolic.toString()
            diastolic = entry.diastolic.toString()
            pulse = entry.pulse?.toString() ?: ""
            notes = entry.notes
            timestamp = entry.timestamp
            existingId = entry.id
        }
    }

    val isValid = systolic.toIntOrNull() != null && diastolic.toIntOrNull() != null

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Edit Blood Pressure" else "Log Blood Pressure",
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
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Blood Pressure Reading",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = systolic,
                    onValueChange = { systolic = it.filter { c -> c.isDigit() } },
                    label = { Text("Systolic") },
                    placeholder = { Text("120") },
                    suffix = { Text("mmHg") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedTextField(
                    value = diastolic,
                    onValueChange = { diastolic = it.filter { c -> c.isDigit() } },
                    label = { Text("Diastolic") },
                    placeholder = { Text("80") },
                    suffix = { Text("mmHg") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

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
                    val sys = systolic.toIntOrNull() ?: return@Button
                    val dia = diastolic.toIntOrNull() ?: return@Button
                    val pul = pulse.toIntOrNull()

                    if (isEditMode && existingId != null) {
                        viewModel.updateBloodPressure(
                            BloodPressureEntry(
                                id = existingId!!,
                                systolic = sys,
                                diastolic = dia,
                                pulse = pul,
                                notes = notes.trim(),
                                timestamp = timestamp
                            )
                        )
                    } else {
                        viewModel.addBloodPressure(
                            systolic = sys,
                            diastolic = dia,
                            pulse = pul,
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
                Text(if (isEditMode) "Update Blood Pressure" else "Save Blood Pressure")
            }
        }
    }
}
