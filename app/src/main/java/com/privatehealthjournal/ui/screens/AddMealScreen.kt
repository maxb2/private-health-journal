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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.BrunchDining
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.privatehealthjournal.data.entity.MealEntry
import com.privatehealthjournal.data.entity.MealType
import com.privatehealthjournal.ui.components.DateTimePicker
import com.privatehealthjournal.viewmodel.LogViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddMealScreen(
    viewModel: LogViewModel,
    onNavigateBack: () -> Unit,
    editId: Long? = null,
    preselectedMealType: String? = null
) {
    val editingMeal by viewModel.editingMeal.collectAsState()
    val isEditMode = editId != null

    val initialMealType = preselectedMealType?.let {
        try { MealType.valueOf(it) } catch (e: IllegalArgumentException) { MealType.BREAKFAST }
    } ?: MealType.BREAKFAST

    var selectedMealType by remember { mutableStateOf(initialMealType) }
    val foods = remember { mutableStateListOf<String>() }
    var currentFood by remember { mutableStateOf("") }
    val tags = remember { mutableStateListOf<String>() }
    var currentTag by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var timestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var existingId by remember { mutableStateOf<Long?>(null) }

    val existingTags by viewModel.allTags.collectAsState()
    val foodNames by viewModel.allFoodNames.collectAsState()

    // Load existing entry for editing
    LaunchedEffect(editId) {
        if (editId != null) {
            viewModel.loadMealForEditing(editId)
        }
    }

    // Populate fields when editing entry is loaded
    LaunchedEffect(editingMeal) {
        editingMeal?.let { mealWithDetails ->
            selectedMealType = mealWithDetails.meal.mealType
            foods.clear()
            foods.addAll(mealWithDetails.foods.map { it.name })
            tags.clear()
            tags.addAll(mealWithDetails.tags.map { it.name })
            notes = mealWithDetails.meal.notes
            timestamp = mealWithDetails.meal.timestamp
            existingId = mealWithDetails.meal.id
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Edit Meal" else "Log Meal",
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
                    containerColor = MaterialTheme.colorScheme.primaryContainer
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
            // Meal Type Selection
            Text(
                text = "Meal Type",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MealType.entries.forEach { mealType ->
                    FilterChip(
                        selected = selectedMealType == mealType,
                        onClick = { selectedMealType = mealType },
                        label = { Text(getMealTypeName(mealType)) },
                        leadingIcon = {
                            Icon(
                                imageVector = getMealIcon(mealType),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Foods Section
            Text(
                text = "Foods",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (foods.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    foods.forEach { food ->
                        InputChip(
                            selected = false,
                            onClick = { },
                            label = { Text(food) },
                            trailingIcon = {
                                IconButton(
                                    onClick = { foods.remove(food) },
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            },
                            colors = InputChipDefaults.inputChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = currentFood,
                    onValueChange = { currentFood = it },
                    label = { Text("Add food item") },
                    placeholder = { Text("e.g., Eggs, Toast") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                IconButton(
                    onClick = {
                        if (currentFood.isNotBlank()) {
                            foods.add(currentFood.trim())
                            currentFood = ""
                        }
                    },
                    enabled = currentFood.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add food"
                    )
                }
            }

            // Food name suggestions
            if (foodNames.isNotEmpty()) {
                val filteredFoods = if (currentFood.isBlank()) {
                    foodNames.filter { it !in foods }.take(8)
                } else {
                    foodNames.filter {
                        it.contains(currentFood, ignoreCase = true) &&
                            !it.equals(currentFood, ignoreCase = true) &&
                            it !in foods
                    }.take(5)
                }
                if (filteredFoods.isNotEmpty()) {
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
                        filteredFoods.forEach { suggestion ->
                            AssistChip(
                                onClick = {
                                    foods.add(suggestion)
                                    currentFood = ""
                                },
                                label = { Text(suggestion) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tags Section
            Text(
                text = "Tags",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tags.forEach { tag ->
                        InputChip(
                            selected = true,
                            onClick = { },
                            label = { Text(tag) },
                            trailingIcon = {
                                IconButton(
                                    onClick = { tags.remove(tag) },
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            },
                            colors = InputChipDefaults.inputChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTrailingIconColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = currentTag,
                    onValueChange = { currentTag = it },
                    label = { Text("Add tag") },
                    placeholder = { Text("e.g., Homemade, Spicy") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                IconButton(
                    onClick = {
                        if (currentTag.isNotBlank() && currentTag.trim() !in tags) {
                            tags.add(currentTag.trim())
                            currentTag = ""
                        }
                    },
                    enabled = currentTag.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add tag"
                    )
                }
            }

            // Show existing tags as suggestions (filtered by typed text)
            if (existingTags.isNotEmpty()) {
                val filteredTags = if (currentTag.isBlank()) {
                    existingTags.filter { it.name !in tags }.take(10)
                } else {
                    existingTags.filter {
                        it.name.contains(currentTag, ignoreCase = true) &&
                            !it.name.equals(currentTag, ignoreCase = true) &&
                            it.name !in tags
                    }.take(5)
                }
                if (filteredTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Suggestions:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        filteredTags.forEach { tag ->
                            AssistChip(
                                onClick = {
                                    tags.add(tag.name)
                                    currentTag = ""
                                },
                                label = { Text(tag.name) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Date/Time Section
            DateTimePicker(
                timestamp = timestamp,
                onTimestampChange = { timestamp = it },
                label = "Date & Time"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Notes
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
                    if (foods.isNotEmpty()) {
                        if (isEditMode && existingId != null) {
                            viewModel.updateMeal(
                                meal = MealEntry(
                                    id = existingId!!,
                                    mealType = selectedMealType,
                                    notes = notes.trim(),
                                    timestamp = timestamp
                                ),
                                foods = foods.toList(),
                                tags = tags.toList()
                            )
                        } else {
                            viewModel.addMeal(
                                mealType = selectedMealType,
                                foods = foods.toList(),
                                tags = tags.toList(),
                                notes = notes.trim(),
                                timestamp = timestamp
                            )
                        }
                        viewModel.clearEditingState()
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = foods.isNotEmpty()
            ) {
                Text(if (isEditMode) "Update Meal" else "Save Meal")
            }
        }
    }
}

private fun getMealTypeName(mealType: MealType): String {
    return when (mealType) {
        MealType.BREAKFAST -> "Breakfast"
        MealType.LUNCH -> "Lunch"
        MealType.DINNER -> "Dinner"
        MealType.SNACK -> "Snack"
    }
}

private fun getMealIcon(mealType: MealType) = when (mealType) {
    MealType.BREAKFAST -> Icons.Default.FreeBreakfast
    MealType.LUNCH -> Icons.Default.BrunchDining
    MealType.DINNER -> Icons.Default.DinnerDining
    MealType.SNACK -> Icons.Default.Cookie
}
