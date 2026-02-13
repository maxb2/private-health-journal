package com.privatehealthjournal.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.privatehealthjournal.data.entity.BristolType
import com.privatehealthjournal.data.entity.OtherEntry
import com.privatehealthjournal.data.entity.OtherEntryType
import com.privatehealthjournal.ui.components.DateTimePicker
import com.privatehealthjournal.viewmodel.LogViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddOtherEntryScreen(
    viewModel: LogViewModel,
    onNavigateBack: () -> Unit,
    editId: Long? = null,
    preselectedType: String? = null
) {
    val editingOtherEntry by viewModel.editingOtherEntry.collectAsState()
    val exerciseTypes by viewModel.exerciseTypes.collectAsState()
    val sleepQualities by viewModel.sleepQualities.collectAsState()
    val stressSources by viewModel.stressSources.collectAsState()
    val moodDescriptions by viewModel.moodDescriptions.collectAsState()
    val otherCategories by viewModel.otherCategories.collectAsState()
    val isEditMode = editId != null

    val initialType = preselectedType?.let {
        try { OtherEntryType.valueOf(it) } catch (e: IllegalArgumentException) { OtherEntryType.BOWEL_MOVEMENT }
    } ?: OtherEntryType.BOWEL_MOVEMENT

    var selectedType by remember { mutableStateOf(initialType) }
    var subType by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var timestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var existingId by remember { mutableStateOf<Long?>(null) }

    // For bowel movement Bristol scale
    var selectedBristolType by remember { mutableIntStateOf(4) }

    // Load existing entry for editing
    LaunchedEffect(editId) {
        if (editId != null) {
            viewModel.loadOtherEntryForEditing(editId)
        }
    }

    // Populate fields when editing entry is loaded
    LaunchedEffect(editingOtherEntry) {
        editingOtherEntry?.let { entry ->
            selectedType = entry.entryType
            subType = entry.subType
            value = entry.value
            notes = entry.notes
            timestamp = entry.timestamp
            existingId = entry.id
            if (entry.entryType == OtherEntryType.BOWEL_MOVEMENT) {
                selectedBristolType = entry.value.toIntOrNull() ?: 4
            }
        }
    }

    val screenTitle = if (isEditMode) {
        "Edit ${getTypeTitle(selectedType)}"
    } else {
        "Log ${getTypeTitle(selectedType)}"
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        screenTitle,
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
            // Type-specific fields
            when (selectedType) {
                OtherEntryType.BOWEL_MOVEMENT -> {
                    BowelMovementFields(
                        selectedBristolType = selectedBristolType,
                        onBristolTypeChange = { selectedBristolType = it }
                    )
                }
                OtherEntryType.SLEEP -> {
                    OutlinedTextField(
                        value = value,
                        onValueChange = { value = it },
                        label = { Text("Hours of Sleep") },
                        placeholder = { Text("e.g., 7.5") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = subType,
                        onValueChange = { subType = it },
                        label = { Text("Quality") },
                        placeholder = { Text("e.g., Good, Poor, Restless") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    SubTypeSuggestions(
                        suggestions = sleepQualities,
                        currentText = subType,
                        onSelect = { subType = it }
                    )
                }
                OtherEntryType.EXERCISE -> {
                    OutlinedTextField(
                        value = subType,
                        onValueChange = { subType = it },
                        label = { Text("Exercise Type") },
                        placeholder = { Text("e.g., Walking, Running, Yoga") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    SubTypeSuggestions(
                        suggestions = exerciseTypes,
                        currentText = subType,
                        onSelect = { subType = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = value,
                        onValueChange = { value = it },
                        label = { Text("Duration") },
                        placeholder = { Text("e.g., 30 minutes") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                OtherEntryType.STRESS -> {
                    OutlinedTextField(
                        value = value,
                        onValueChange = { value = it },
                        label = { Text("Stress Level (1-10)") },
                        placeholder = { Text("e.g., 7") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = subType,
                        onValueChange = { subType = it },
                        label = { Text("Source") },
                        placeholder = { Text("e.g., Work, Family") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    SubTypeSuggestions(
                        suggestions = stressSources,
                        currentText = subType,
                        onSelect = { subType = it }
                    )
                }
                OtherEntryType.MOOD -> {
                    OutlinedTextField(
                        value = value,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                value = newValue
                            }
                        },
                        label = { Text("Mood Level (1-10)") },
                        placeholder = { Text("e.g., 7") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = subType,
                        onValueChange = { subType = it },
                        label = { Text("Description") },
                        placeholder = { Text("e.g., Happy, Anxious, Calm") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    SubTypeSuggestions(
                        suggestions = moodDescriptions,
                        currentText = subType,
                        onSelect = { subType = it }
                    )
                }
                OtherEntryType.WATER_INTAKE -> {
                    OutlinedTextField(
                        value = value,
                        onValueChange = { value = it },
                        label = { Text("Amount") },
                        placeholder = { Text("e.g., 8 glasses, 2 liters") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                OtherEntryType.OTHER -> {
                    OutlinedTextField(
                        value = subType,
                        onValueChange = { subType = it },
                        label = { Text("Category") },
                        placeholder = { Text("e.g., Supplement, Event") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    SubTypeSuggestions(
                        suggestions = otherCategories,
                        currentText = subType,
                        onSelect = { subType = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = value,
                        onValueChange = { value = it },
                        label = { Text("Value") },
                        placeholder = { Text("Details...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date/Time Section
            DateTimePicker(
                timestamp = timestamp,
                onTimestampChange = { timestamp = it },
                label = "Date & Time"
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                    val finalValue = if (selectedType == OtherEntryType.BOWEL_MOVEMENT) {
                        selectedBristolType.toString()
                    } else {
                        value.trim()
                    }

                    val entry = OtherEntry(
                        id = if (isEditMode) existingId ?: 0 else 0,
                        entryType = selectedType,
                        subType = subType.trim(),
                        value = finalValue,
                        notes = notes.trim(),
                        timestamp = timestamp
                    )

                    if (isEditMode && existingId != null) {
                        viewModel.updateOtherEntry(entry)
                    } else {
                        viewModel.addOtherEntry(entry)
                    }
                    viewModel.clearEditingState()
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Text(if (isEditMode) "Update Entry" else "Save Entry")
            }

            // Bristol scale guide for bowel movements
            if (selectedType == OtherEntryType.BOWEL_MOVEMENT) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Guide",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Types 1-2: May indicate constipation\nTypes 3-4: Ideal, normal stools\nTypes 5-7: May indicate diarrhea",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BowelMovementFields(
    selectedBristolType: Int,
    onBristolTypeChange: (Int) -> Unit
) {
    Text(
        text = "Bristol Stool Scale",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = "Select the type that best matches",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    )

    Spacer(modifier = Modifier.height(16.dp))

    BristolType.entries.forEach { bristolType ->
        BristolTypeCard(
            bristolType = bristolType,
            isSelected = selectedBristolType == bristolType.type,
            onClick = { onBristolTypeChange(bristolType.type) }
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun BristolTypeCard(
    bristolType: BristolType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val typeColor = getBristolTypeColor(bristolType.type)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                typeColor.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, typeColor)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.size(48.dp)
            ) {
                Text(
                    text = bristolType.type.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = typeColor
                )
                Row {
                    repeat(getBristolVisualCount(bristolType.type)) {
                        Icon(
                            imageVector = Icons.Filled.Circle,
                            contentDescription = null,
                            tint = typeColor,
                            modifier = Modifier.size(getBristolVisualSize(bristolType.type))
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bristolType.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = bristolType.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun getBristolTypeColor(type: Int): Color {
    return when (type) {
        1, 2 -> MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
        3, 4 -> Color(0xFF4CAF50)
        5 -> Color(0xFFFFC107)
        6, 7 -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }
}

private fun getBristolVisualCount(type: Int): Int {
    return when (type) {
        1 -> 4; 2 -> 3; 3 -> 2; 4 -> 1; 5 -> 3; 6 -> 4; 7 -> 5; else -> 1
    }
}

private fun getBristolVisualSize(type: Int) = when (type) {
    1 -> 8.dp; 2 -> 10.dp; 7 -> 6.dp; else -> 8.dp
}

private fun getTypeTitle(type: OtherEntryType): String {
    return when (type) {
        OtherEntryType.BOWEL_MOVEMENT -> "Bowel Movement"
        OtherEntryType.SLEEP -> "Sleep"
        OtherEntryType.EXERCISE -> "Exercise"
        OtherEntryType.STRESS -> "Stress"
        OtherEntryType.MOOD -> "Mood"
        OtherEntryType.WATER_INTAKE -> "Water Intake"
        OtherEntryType.OTHER -> "Other"
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SubTypeSuggestions(
    suggestions: List<String>,
    currentText: String,
    onSelect: (String) -> Unit
) {
    if (suggestions.isEmpty()) return
    val filtered = if (currentText.isBlank()) {
        suggestions.take(8)
    } else {
        suggestions.filter {
            it.contains(currentText, ignoreCase = true) &&
                !it.equals(currentText, ignoreCase = true)
        }.take(5)
    }
    if (filtered.isEmpty()) return
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Suggestions:",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    )
    Spacer(modifier = Modifier.height(4.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        filtered.forEach { suggestion ->
            AssistChip(
                onClick = { onSelect(suggestion) },
                label = { Text(suggestion) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

