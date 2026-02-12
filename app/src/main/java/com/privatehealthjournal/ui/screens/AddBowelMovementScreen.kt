package com.privatehealthjournal.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import com.privatehealthjournal.data.entity.BowelMovementEntry
import com.privatehealthjournal.data.entity.BristolType
import com.privatehealthjournal.ui.components.DateTimePicker
import com.privatehealthjournal.viewmodel.LogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBowelMovementScreen(
    viewModel: LogViewModel,
    onNavigateBack: () -> Unit,
    editId: Long? = null
) {
    val editingBowelMovement by viewModel.editingBowelMovement.collectAsState()
    val isEditMode = editId != null

    var selectedType by remember { mutableIntStateOf(4) }
    var notes by remember { mutableStateOf("") }
    var timestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var existingId by remember { mutableStateOf<Long?>(null) }

    // Load existing entry for editing
    LaunchedEffect(editId) {
        if (editId != null) {
            viewModel.loadBowelMovementForEditing(editId)
        }
    }

    // Populate fields when editing entry is loaded
    LaunchedEffect(editingBowelMovement) {
        editingBowelMovement?.let { entry ->
            selectedType = entry.bristolType
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
                        if (isEditMode) "Edit Bowel Movement" else "Log Bowel Movement",
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
                    isSelected = selectedType == bristolType.type,
                    onClick = { selectedType = bristolType.type }
                )
                Spacer(modifier = Modifier.height(8.dp))
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
                    if (isEditMode && existingId != null) {
                        viewModel.updateBowelMovement(
                            BowelMovementEntry(
                                id = existingId!!,
                                bristolType = selectedType,
                                notes = notes.trim(),
                                timestamp = timestamp
                            )
                        )
                    } else {
                        viewModel.addBowelMovement(selectedType, notes.trim(), timestamp)
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

            Spacer(modifier = Modifier.height(16.dp))

            // Legend
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
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
            // Visual indicator
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
        1 -> 4
        2 -> 3
        3 -> 2
        4 -> 1
        5 -> 3
        6 -> 4
        7 -> 5
        else -> 1
    }
}

private fun getBristolVisualSize(type: Int) = when (type) {
    1 -> 8.dp
    2 -> 10.dp
    7 -> 6.dp
    else -> 8.dp
}
