package com.privatehealthjournal.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.BrunchDining
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.privatehealthjournal.data.entity.BowelMovementEntry
import com.privatehealthjournal.data.entity.BristolType
import com.privatehealthjournal.data.entity.MealType
import com.privatehealthjournal.data.entity.MealWithDetails
import com.privatehealthjournal.data.entity.OtherEntry
import com.privatehealthjournal.data.entity.OtherEntryType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MealEntryCard(
    meal: MealWithDetails,
    onDelete: () -> Unit,
    onEdit: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = getMealIcon(meal.meal.mealType),
                contentDescription = meal.meal.mealType.name,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getMealTypeName(meal.meal.mealType),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (meal.foods.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = meal.foods.joinToString(", ") { it.name },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                if (meal.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        meal.tags.forEach { tag ->
                            AssistChip(
                                onClick = { },
                                label = {
                                    Text(
                                        text = tag.name,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }
                }

                if (meal.meal.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = meal.meal.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimestamp(meal.meal.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                )
            }
            Column {
                if (onEdit != null) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun SymptomEntryCard(
    name: String,
    severity: Int,
    notes: String,
    startTime: Long,
    endTime: Long?,
    onDelete: () -> Unit,
    onEdit: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Symptom",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    SeverityIndicator(severity = severity)
                    if (endTime == null) {
                        OngoingIndicator()
                    }
                }
                if (notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimeRange(startTime, endTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                )
            }
            Column {
                if (onEdit != null) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                        )
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun BowelMovementCard(
    entry: BowelMovementEntry,
    onDelete: () -> Unit,
    onEdit: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val bristolType = BristolType.fromInt(entry.bristolType)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.size(40.dp)
            ) {
                Text(
                    text = entry.bristolType.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = getBristolColor(entry.bristolType)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Bowel Movement",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    BristolIndicator(type = entry.bristolType)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = bristolType.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
                if (entry.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = entry.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimestamp(entry.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.5f)
                )
            }
            Column {
                if (onEdit != null) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f)
                        )
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun OtherEntryCard(
    entry: OtherEntry,
    onDelete: () -> Unit,
    onEdit: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val icon = when (entry.entryType) {
        OtherEntryType.BOWEL_MOVEMENT -> Icons.Default.WaterDrop
        OtherEntryType.SLEEP -> Icons.Default.Bedtime
        OtherEntryType.EXERCISE -> Icons.Default.FitnessCenter
        OtherEntryType.STRESS -> Icons.Default.Psychology
        OtherEntryType.WATER_INTAKE -> Icons.Default.WaterDrop
        OtherEntryType.OTHER -> Icons.Default.MoreHoriz
    }

    val title = when (entry.entryType) {
        OtherEntryType.BOWEL_MOVEMENT -> "Bowel Movement"
        OtherEntryType.SLEEP -> "Sleep"
        OtherEntryType.EXERCISE -> "Exercise"
        OtherEntryType.STRESS -> "Stress"
        OtherEntryType.WATER_INTAKE -> "Water Intake"
        OtherEntryType.OTHER -> entry.subType.ifBlank { "Other" }
    }

    val subtitle = when (entry.entryType) {
        OtherEntryType.BOWEL_MOVEMENT -> {
            val bristolType = entry.value.toIntOrNull()?.let { BristolType.fromInt(it) }
            bristolType?.let { "Type ${it.type}: ${it.description}" } ?: ""
        }
        OtherEntryType.SLEEP -> buildString {
            if (entry.value.isNotBlank()) append("${entry.value} hours")
            if (entry.subType.isNotBlank()) {
                if (isNotEmpty()) append(" - ")
                append(entry.subType)
            }
        }
        OtherEntryType.EXERCISE -> buildString {
            if (entry.subType.isNotBlank()) append(entry.subType)
            if (entry.value.isNotBlank()) {
                if (isNotEmpty()) append(" - ")
                append(entry.value)
            }
        }
        OtherEntryType.STRESS -> buildString {
            if (entry.value.isNotBlank()) append("Level: ${entry.value}")
            if (entry.subType.isNotBlank()) {
                if (isNotEmpty()) append(" - ")
                append(entry.subType)
            }
        }
        OtherEntryType.WATER_INTAKE -> entry.value
        OtherEntryType.OTHER -> entry.value
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (entry.entryType == OtherEntryType.BOWEL_MOVEMENT) {
                val bristolValue = entry.value.toIntOrNull() ?: 4
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.size(40.dp)
                ) {
                    Text(
                        text = bristolValue.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = getBristolColor(bristolValue)
                    )
                }
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (entry.entryType == OtherEntryType.BOWEL_MOVEMENT) {
                        val bristolValue = entry.value.toIntOrNull() ?: 4
                        BristolIndicator(type = bristolValue)
                    }
                }
                if (subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    )
                }
                if (entry.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = entry.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimestamp(entry.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.5f)
                )
            }
            Column {
                if (onEdit != null) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f)
                        )
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BristolIndicator(type: Int) {
    val label = when (type) {
        1, 2 -> "Constipation"
        3, 4 -> "Normal"
        5, 6, 7 -> "Loose"
        else -> ""
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = getBristolColor(type)
    )
}

@Composable
private fun getBristolColor(type: Int) = when (type) {
    1, 2 -> MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
    3, 4 -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.error
}

@Composable
private fun SeverityIndicator(severity: Int) {
    Text(
        text = "Severity: $severity/5",
        style = MaterialTheme.typography.labelSmall,
        color = when (severity) {
            1, 2 -> MaterialTheme.colorScheme.tertiary
            3 -> MaterialTheme.colorScheme.secondary
            else -> MaterialTheme.colorScheme.error
        }
    )
}

@Composable
private fun OngoingIndicator() {
    Text(
        text = "Ongoing",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary
    )
}

private fun getMealIcon(mealType: MealType): ImageVector {
    return when (mealType) {
        MealType.BREAKFAST -> Icons.Default.FreeBreakfast
        MealType.LUNCH -> Icons.Default.BrunchDining
        MealType.DINNER -> Icons.Default.DinnerDining
        MealType.SNACK -> Icons.Default.Cookie
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

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatTimeRange(startTime: Long, endTime: Long?): String {
    val dateSdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    val timeSdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    val fullSdf = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())

    val startDate = Date(startTime)

    return if (endTime == null) {
        "Started ${fullSdf.format(startDate)}"
    } else {
        val endDate = Date(endTime)
        val sameDay = dateSdf.format(startDate) == dateSdf.format(endDate)

        if (sameDay) {
            "${dateSdf.format(startDate)}, ${timeSdf.format(startDate)} - ${timeSdf.format(endDate)}"
        } else {
            "${fullSdf.format(startDate)} - ${fullSdf.format(endDate)}"
        }
    }
}
