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
import com.privatehealthjournal.data.entity.CholesterolEntry
import com.privatehealthjournal.ui.components.DateTimePicker
import com.privatehealthjournal.viewmodel.LogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCholesterolScreen(
    viewModel: LogViewModel,
    onNavigateBack: () -> Unit,
    editId: Long? = null
) {
    val editingEntry by viewModel.editingCholesterol.collectAsState()
    val isEditMode = editId != null

    var total by remember { mutableStateOf("") }
    var ldl by remember { mutableStateOf("") }
    var hdl by remember { mutableStateOf("") }
    var triglycerides by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var timestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var existingId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(editId) {
        if (editId != null) {
            viewModel.loadCholesterolForEditing(editId)
        }
    }

    LaunchedEffect(editingEntry) {
        editingEntry?.let { entry ->
            total = entry.total?.toString() ?: ""
            ldl = entry.ldl?.toString() ?: ""
            hdl = entry.hdl?.toString() ?: ""
            triglycerides = entry.triglycerides?.toString() ?: ""
            notes = entry.notes
            timestamp = entry.timestamp
            existingId = entry.id
        }
    }

    val hasAtLeastOneValue = total.isNotBlank() || ldl.isNotBlank() || hdl.isNotBlank() || triglycerides.isNotBlank()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Edit Cholesterol" else "Log Cholesterol",
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
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Cholesterol Levels",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "Enter at least one value",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = total,
                    onValueChange = { total = it.filter { c -> c.isDigit() } },
                    label = { Text("Total") },
                    placeholder = { Text("200") },
                    suffix = { Text("mg/dL") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedTextField(
                    value = ldl,
                    onValueChange = { ldl = it.filter { c -> c.isDigit() } },
                    label = { Text("LDL") },
                    placeholder = { Text("100") },
                    suffix = { Text("mg/dL") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = hdl,
                    onValueChange = { hdl = it.filter { c -> c.isDigit() } },
                    label = { Text("HDL") },
                    placeholder = { Text("60") },
                    suffix = { Text("mg/dL") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedTextField(
                    value = triglycerides,
                    onValueChange = { triglycerides = it.filter { c -> c.isDigit() } },
                    label = { Text("Triglycerides") },
                    placeholder = { Text("150") },
                    suffix = { Text("mg/dL") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
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
                    val entry = CholesterolEntry(
                        id = existingId ?: 0,
                        total = total.toIntOrNull(),
                        ldl = ldl.toIntOrNull(),
                        hdl = hdl.toIntOrNull(),
                        triglycerides = triglycerides.toIntOrNull(),
                        notes = notes.trim(),
                        timestamp = timestamp
                    )

                    if (isEditMode && existingId != null) {
                        viewModel.updateCholesterol(entry)
                    } else {
                        viewModel.addCholesterol(
                            total = total.toIntOrNull(),
                            ldl = ldl.toIntOrNull(),
                            hdl = hdl.toIntOrNull(),
                            triglycerides = triglycerides.toIntOrNull(),
                            notes = notes.trim(),
                            timestamp = timestamp
                        )
                    }
                    viewModel.clearEditingState()
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = hasAtLeastOneValue
            ) {
                Text(if (isEditMode) "Update Cholesterol" else "Save Cholesterol")
            }
        }
    }
}
