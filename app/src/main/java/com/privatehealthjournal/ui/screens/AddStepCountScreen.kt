package com.privatehealthjournal.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.privatehealthjournal.data.entity.StepCountEntry
import com.privatehealthjournal.ui.components.DatePickerDialogWrapper
import com.privatehealthjournal.ui.components.formatDate
import com.privatehealthjournal.ui.components.EntryTopAppBar
import com.privatehealthjournal.ui.components.rememberEditingEntry
import com.privatehealthjournal.viewmodel.StepsViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStepCountScreen(
    viewModel: StepsViewModel,
    onNavigateBack: () -> Unit,
    editId: Long? = null
) {
    val isEditMode = editId != null

    var steps by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var dateMillis by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
    // existingEntry / showDatePicker stay on `remember` — StepCountEntry has no Saver and
    // the picker flag is transient UI state.
    var existingEntry by remember { mutableStateOf<StepCountEntry?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    rememberEditingEntry(
        editId = editId,
        editingFlow = viewModel.editingStepCount,
        load = { viewModel.loadStepCountForEditing(it) }
    ) { entry ->
        steps = entry.steps.toString()
        notes = entry.notes
        val date = LocalDate.ofEpochDay(entry.dateEpochDay)
        dateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        existingEntry = entry
    }

    val stepsValue = steps.toIntOrNull()
    val isValid = stepsValue != null && stepsValue >= 0

    val handleBack = {
        viewModel.clearEditingStepCount()
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            EntryTopAppBar(
                title = if (isEditMode) "Edit Steps" else "Log Steps",
                onBack = handleBack,
                containerColor = com.privatehealthjournal.ui.theme.biometricContainerColor()
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
                imageVector = Icons.Default.DirectionsWalk,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Daily Step Count",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = steps,
                onValueChange = { steps = it.filter { c -> c.isDigit() }.take(7) },
                label = { Text("Steps") },
                placeholder = { Text("8000") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Date",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Select date",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDate(dateMillis),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val stepsVal = steps.toIntOrNull() ?: return@Button
                    val epochDay = Instant.ofEpochMilli(dateMillis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .toEpochDay()

                    val existing = existingEntry
                    if (isEditMode && existing != null) {
                        viewModel.updateStepCount(
                            existing.copy(
                                dateEpochDay = epochDay,
                                steps = stepsVal,
                                notes = notes.trim()
                            )
                        )
                    } else {
                        viewModel.addStepCount(epochDay, stepsVal, notes.trim())
                    }
                    viewModel.clearEditingStepCount()
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isValid
            ) {
                Text(if (isEditMode) "Update Steps" else "Save Steps")
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialogWrapper(
            initialTimestamp = dateMillis,
            onDismiss = { showDatePicker = false },
            onConfirm = { selectedDate ->
                dateMillis = selectedDate
                showDatePicker = false
            }
        )
    }
}
