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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.privatehealthjournal.viewmodel.LogViewModel

private data class MedItemState(
    var name: String = "",
    var dosage: String = ""
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddMedicationSetScreen(
    viewModel: LogViewModel,
    onNavigateBack: () -> Unit,
    editId: Long? = null
) {
    val editingSet by viewModel.editingMedicationSet.collectAsState()
    val medicationNames by viewModel.allMedicationNames.collectAsState()
    val isEditMode = editId != null

    var setName by remember { mutableStateOf("") }
    val items = remember { mutableStateListOf(MedItemState()) }
    var existingId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(editId) {
        if (editId != null) {
            viewModel.loadMedicationSetForEditing(editId)
        }
    }

    LaunchedEffect(editingSet) {
        editingSet?.let { setWithItems ->
            setName = setWithItems.set.name
            existingId = setWithItems.set.id
            items.clear()
            setWithItems.items.forEach { item ->
                items.add(MedItemState(name = item.name, dosage = item.dosage))
            }
            if (items.isEmpty()) {
                items.add(MedItemState())
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Edit Medication Set" else "New Medication Set",
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
                value = setName,
                onValueChange = { setName = it },
                label = { Text("Set Name") },
                placeholder = { Text("e.g., Morning Meds") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Medications",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            items.forEachIndexed { index, item ->
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = item.name,
                                onValueChange = { newValue ->
                                    items[index] = item.copy(name = newValue)
                                },
                                label = { Text("Medication Name") },
                                placeholder = { Text("e.g., Metformin") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            // Autocomplete suggestions
                            if (medicationNames.isNotEmpty() && item.name.isNotBlank()) {
                                val filtered = medicationNames.filter {
                                    it.contains(item.name, ignoreCase = true) && !it.equals(item.name, ignoreCase = true)
                                }
                                if (filtered.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        filtered.take(3).forEach { suggestion ->
                                            AssistChip(
                                                onClick = { items[index] = item.copy(name = suggestion) },
                                                label = { Text(suggestion, style = MaterialTheme.typography.bodySmall) },
                                                colors = AssistChipDefaults.assistChipColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = item.dosage,
                                onValueChange = { newValue ->
                                    items[index] = item.copy(dosage = newValue)
                                },
                                label = { Text("Dosage") },
                                placeholder = { Text("e.g., 500mg") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        if (items.size > 1) {
                            IconButton(
                                onClick = { items.removeAt(index) },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Remove medication",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(48.dp))
                        }
                    }

                    if (index < items.lastIndex) {
                        Divider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { items.add(MedItemState()) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Medication")
            }

            Spacer(modifier = Modifier.height(24.dp))

            val hasValidItems = items.any { it.name.isNotBlank() }

            Button(
                onClick = {
                    val validItems = items
                        .filter { it.name.isNotBlank() }
                        .map { it.name.trim() to it.dosage.trim() }

                    if (isEditMode && existingId != null) {
                        viewModel.updateMedicationSet(existingId!!, setName.trim(), validItems)
                    } else {
                        viewModel.addMedicationSet(setName.trim(), validItems)
                    }
                    viewModel.clearEditingState()
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = setName.isNotBlank() && hasValidItems,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Text(if (isEditMode) "Update Medication Set" else "Save Medication Set")
            }
        }
    }
}
