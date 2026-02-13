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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.privatehealthjournal.data.entity.DaysOfWeek
import com.privatehealthjournal.data.entity.MedicationSetReminder

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReminderEditDialog(
    existingReminder: MedicationSetReminder? = null,
    setId: Long,
    onSave: (MedicationSetReminder) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = existingReminder?.hour ?: 8,
        initialMinute = existingReminder?.minute ?: 0,
        is24Hour = false
    )
    var selectedDays by remember {
        mutableIntStateOf(existingReminder?.daysOfWeek ?: DaysOfWeek.EVERY_DAY)
    }

    val dayOptions = listOf(
        DaysOfWeek.MONDAY to "Mon",
        DaysOfWeek.TUESDAY to "Tue",
        DaysOfWeek.WEDNESDAY to "Wed",
        DaysOfWeek.THURSDAY to "Thu",
        DaysOfWeek.FRIDAY to "Fri",
        DaysOfWeek.SATURDAY to "Sat",
        DaysOfWeek.SUNDAY to "Sun"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (existingReminder != null) "Edit Reminder" else "Add Reminder")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(state = timePickerState)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Repeat on",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    dayOptions.forEach { (dayBit, label) ->
                        val isSelected = DaysOfWeek.isDayEnabled(selectedDays, dayBit)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedDays = if (isSelected) {
                                    selectedDays and dayBit.inv()
                                } else {
                                    selectedDays or dayBit
                                }
                            },
                            label = { Text(label) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedDays == 0) return@TextButton
                    val reminder = MedicationSetReminder(
                        id = existingReminder?.id ?: 0,
                        setId = setId,
                        hour = timePickerState.hour,
                        minute = timePickerState.minute,
                        daysOfWeek = selectedDays,
                        enabled = existingReminder?.enabled ?: true
                    )
                    onSave(reminder)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
