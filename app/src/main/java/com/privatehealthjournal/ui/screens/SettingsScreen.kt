package com.privatehealthjournal.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import com.privatehealthjournal.sensor.HealthConnectStepReader
import com.privatehealthjournal.viewmodel.LogViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: LogViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val allMeals by viewModel.allMeals.collectAsState()
    val allSymptoms by viewModel.allSymptomEntries.collectAsState()
    val allMedications by viewModel.allMedications.collectAsState()
    val allOtherEntries by viewModel.allOtherEntries.collectAsState()
    val dailyBudget by viewModel.dailyBudget.collectAsState()
    val showCycleTracking by viewModel.showCycleTracking.collectAsState()
    val showStepCounting by viewModel.showStepCounting.collectAsState()
    val stepSensorEnabled by viewModel.stepSensorEnabled.collectAsState()
    val healthConnectEnabled by viewModel.healthConnectEnabled.collectAsState()
    val stepsPerPointCredit by viewModel.stepsPerPointCredit.collectAsState()

    var budgetText by remember { mutableStateOf("") }
    var stepsPerCreditText by remember { mutableStateOf("") }

    LaunchedEffect(dailyBudget) {
        budgetText = dailyBudget?.toString() ?: ""
    }

    LaunchedEffect(stepsPerPointCredit) {
        stepsPerCreditText = stepsPerPointCredit?.toString() ?: ""
    }

    val activityRecognitionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.setStepSensorEnabled(granted)
        if (!granted) {
            Toast.makeText(context, "Activity recognition permission required", Toast.LENGTH_SHORT).show()
        }
    }

    val hcPermissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        val ok = granted.containsAll(HealthConnectStepReader.REQUIRED_PERMISSIONS)
        viewModel.setHealthConnectEnabled(ok)
        if (!ok) {
            Toast.makeText(context, "Health Connect permission required", Toast.LENGTH_SHORT).show()
        }
    }

    val totalEntries = allMeals.size + allSymptoms.size + allMedications.size + allOtherEntries.size

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            viewModel.exportData(it) { success, message ->
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            viewModel.importData(it) { success, message ->
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Settings",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
            // Data summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Your Data",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$totalEntries total entries",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "${allMeals.size} meals, ${allSymptoms.size} symptoms, ${allMedications.size} medications, ${allOtherEntries.size} other",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Meal Budget settings
            Text(
                text = "Meal Budget",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Set your daily point budget. A weekly overage of one extra day's budget is included for flexibility.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = budgetText,
                onValueChange = { newVal ->
                    if (newVal.isEmpty() || newVal.all { it.isDigit() }) {
                        budgetText = newVal
                    }
                },
                label = { Text("Daily Point Budget") },
                placeholder = { Text("e.g., 30") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { viewModel.saveDailyBudget(budgetText.toIntOrNull()) }
                ),
                supportingText = if (budgetText.isNotBlank()) {
                    val daily = budgetText.toIntOrNull()
                    if (daily != null) {
                        { Text("Weekly total: ${daily * 8} pts (${daily * 7} + ${daily} overage)") }
                    } else null
                } else null
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { viewModel.saveDailyBudget(budgetText.toIntOrNull()) },
                modifier = Modifier.fillMaxWidth(),
                enabled = budgetText.toIntOrNull() != null || (budgetText.isEmpty() && dailyBudget != null)
            ) {
                Text(if (budgetText.isEmpty() && dailyBudget != null) "Clear Budget" else "Save Budget")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = stepsPerCreditText,
                onValueChange = { newVal ->
                    if (newVal.isEmpty() || newVal.all { it.isDigit() }) {
                        stepsPerCreditText = newVal
                    }
                },
                label = { Text("Steps per point credit") },
                placeholder = { Text("e.g., 100") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { viewModel.saveStepsPerPointCredit(stepsPerCreditText.toIntOrNull()) }
                ),
                supportingText = { Text("How many steps earn one credit. Leave blank to disable.") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.saveStepsPerPointCredit(stepsPerCreditText.toIntOrNull()) },
                modifier = Modifier.fillMaxWidth(),
                enabled = stepsPerCreditText.toIntOrNull()?.let { it > 0 } == true ||
                    (stepsPerCreditText.isEmpty() && stepsPerPointCredit != null)
            ) {
                Text(
                    if (stepsPerCreditText.isEmpty() && stepsPerPointCredit != null)
                        "Clear" else "Save"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Step Counting",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enable step counting",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Track daily step counts on Home, History, Calendar and charts.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = showStepCounting,
                            onCheckedChange = { viewModel.setShowStepCounting(it) }
                        )
                    }

                    if (showStepCounting) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Use device step sensor",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "Read built-in pedometer when app opens.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = stepSensorEnabled,
                                onCheckedChange = { wantOn ->
                                    if (wantOn) {
                                        activityRecognitionLauncher.launch(
                                            android.Manifest.permission.ACTIVITY_RECOGNITION
                                        )
                                    } else {
                                        viewModel.setStepSensorEnabled(false)
                                    }
                                }
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Use Health Connect",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "Pull steps from Google Health Connect.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = healthConnectEnabled,
                                onCheckedChange = { wantOn ->
                                    if (wantOn) {
                                        val status = HealthConnectClient.getSdkStatus(context)
                                        if (status != HealthConnectClient.SDK_AVAILABLE) {
                                            Toast.makeText(
                                                context,
                                                "Install Health Connect to enable.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            viewModel.setHealthConnectEnabled(false)
                                        } else {
                                            hcPermissionLauncher.launch(
                                                HealthConnectStepReader.REQUIRED_PERMISSIONS
                                            )
                                        }
                                    } else {
                                        viewModel.setHealthConnectEnabled(false)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Display",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cycle Tracking",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Show cycle/period tracking features",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = showCycleTracking,
                        onCheckedChange = { viewModel.setShowCycleTracking(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Import & Export",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Export your data as a JSON file for backup or transfer to another device.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    exportLauncher.launch("private-health-journal-$dateStr.json")
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = totalEntries > 0
            ) {
                Icon(
                    imageVector = Icons.Default.FileUpload,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Export Data")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Import data from a previously exported JSON file. Imported entries are added alongside existing data.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    importLauncher.launch(arrayOf("application/json"))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Import Data")
            }
        }
    }
}
