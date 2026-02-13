package com.privatehealthjournal

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.privatehealthjournal.ui.screens.AddBloodGlucoseScreen
import com.privatehealthjournal.ui.screens.AddBloodPressureScreen
import com.privatehealthjournal.ui.screens.AddCholesterolScreen
import com.privatehealthjournal.ui.screens.AddMealScreen
import com.privatehealthjournal.ui.screens.AddMedicationScreen
import com.privatehealthjournal.ui.screens.AddMedicationSetScreen
import com.privatehealthjournal.ui.screens.AddOtherEntryScreen
import com.privatehealthjournal.ui.screens.AddSpO2Screen
import com.privatehealthjournal.ui.screens.AddSymptomScreen
import com.privatehealthjournal.ui.screens.AddWeightScreen
import com.privatehealthjournal.ui.screens.BiometricsChartScreen
import com.privatehealthjournal.ui.screens.CalendarScreen
import com.privatehealthjournal.ui.screens.HistoryScreen
import com.privatehealthjournal.ui.screens.HomeScreen
import com.privatehealthjournal.ui.screens.MedicationSetsScreen
import com.privatehealthjournal.ui.screens.SettingsScreen
import com.privatehealthjournal.notification.ReminderBroadcastReceiver
import com.privatehealthjournal.ui.theme.PrivateHealthJournalTheme
import com.privatehealthjournal.viewmodel.LogViewModel

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _: Boolean -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        requestNotificationPermission()

        val navigateTo = intent?.getStringExtra("navigate_to")

        setContent {
            PrivateHealthJournalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val viewModel: LogViewModel = viewModel()

                    LaunchedEffect(navigateTo) {
                        if (navigateTo != null) {
                            navController.navigate(navigateTo)
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(
                                viewModel = viewModel,
                                onAddMeal = { navController.navigate("add_meal") },
                                onAddSymptom = { name ->
                                    if (name != null) navController.navigate("add_symptom?name=$name")
                                    else navController.navigate("add_symptom")
                                },
                                onAddOther = { otherType -> navController.navigate("add_other?type=$otherType") },
                                onAddMedication = { name ->
                                    if (name != null) navController.navigate("add_medication?name=$name")
                                    else navController.navigate("add_medication")
                                },
                                onAddBloodPressure = { navController.navigate("add_blood_pressure") },
                                onAddCholesterol = { navController.navigate("add_cholesterol") },
                                onAddWeight = { navController.navigate("add_weight") },
                                onAddSpO2 = { navController.navigate("add_spo2") },
                                onAddBloodGlucose = { navController.navigate("add_blood_glucose") },
                                onViewBiometricsChart = { navController.navigate("biometrics_chart") },
                                onViewHistory = { navController.navigate("history") },
                                onViewCalendar = { navController.navigate("calendar") },
                                onViewSettings = { navController.navigate("settings") },
                                onEditMeal = { id -> navController.navigate("edit_meal/$id") },
                                onEditSymptom = { id -> navController.navigate("edit_symptom/$id") },
                                onEditOther = { id -> navController.navigate("edit_other/$id") },
                                onEditMedication = { id -> navController.navigate("edit_medication/$id") },
                                onEditBloodPressure = { id -> navController.navigate("edit_blood_pressure/$id") },
                                onEditCholesterol = { id -> navController.navigate("edit_cholesterol/$id") },
                                onEditWeight = { id -> navController.navigate("edit_weight/$id") },
                                onEditSpO2 = { id -> navController.navigate("edit_spo2/$id") },
                                onEditBloodGlucose = { id -> navController.navigate("edit_blood_glucose/$id") },
                                onViewMedicationSets = { navController.navigate("medication_sets") }
                            )
                        }
                        composable(
                            route = "add_meal?mealType={mealType}",
                            arguments = listOf(navArgument("mealType") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            })
                        ) { backStackEntry ->
                            val mealType = backStackEntry.arguments?.getString("mealType")
                            AddMealScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                preselectedMealType = mealType
                            )
                        }
                        composable(
                            route = "add_symptom?name={name}",
                            arguments = listOf(navArgument("name") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            })
                        ) { backStackEntry ->
                            val prefillName = backStackEntry.arguments?.getString("name")
                            AddSymptomScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                prefillName = prefillName
                            )
                        }
                        composable(
                            route = "add_other?type={type}",
                            arguments = listOf(navArgument("type") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            })
                        ) { backStackEntry ->
                            val otherType = backStackEntry.arguments?.getString("type")
                            AddOtherEntryScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                preselectedType = otherType
                            )
                        }
                        composable(
                            route = "add_medication?name={name}",
                            arguments = listOf(navArgument("name") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            })
                        ) { backStackEntry ->
                            val prefillName = backStackEntry.arguments?.getString("name")
                            AddMedicationScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                prefillName = prefillName
                            )
                        }
                        composable("add_blood_pressure") {
                            AddBloodPressureScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("add_cholesterol") {
                            AddCholesterolScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("add_weight") {
                            AddWeightScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("add_spo2") {
                            AddSpO2Screen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("add_blood_glucose") {
                            AddBloodGlucoseScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("biometrics_chart") {
                            BiometricsChartScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("calendar") {
                            CalendarScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onEditMeal = { id -> navController.navigate("edit_meal/$id") },
                                onEditSymptom = { id -> navController.navigate("edit_symptom/$id") },
                                onEditOther = { id -> navController.navigate("edit_other/$id") },
                                onEditMedication = { id -> navController.navigate("edit_medication/$id") },
                                onEditBloodPressure = { id -> navController.navigate("edit_blood_pressure/$id") },
                                onEditCholesterol = { id -> navController.navigate("edit_cholesterol/$id") },
                                onEditWeight = { id -> navController.navigate("edit_weight/$id") },
                                onEditSpO2 = { id -> navController.navigate("edit_spo2/$id") },
                                onEditBloodGlucose = { id -> navController.navigate("edit_blood_glucose/$id") }
                            )
                        }
                        composable("history") {
                            HistoryScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onEditMeal = { id -> navController.navigate("edit_meal/$id") },
                                onEditSymptom = { id -> navController.navigate("edit_symptom/$id") },
                                onEditOther = { id -> navController.navigate("edit_other/$id") },
                                onEditMedication = { id -> navController.navigate("edit_medication/$id") },
                                onEditBloodPressure = { id -> navController.navigate("edit_blood_pressure/$id") },
                                onEditCholesterol = { id -> navController.navigate("edit_cholesterol/$id") },
                                onEditWeight = { id -> navController.navigate("edit_weight/$id") },
                                onEditSpO2 = { id -> navController.navigate("edit_spo2/$id") },
                                onEditBloodGlucose = { id -> navController.navigate("edit_blood_glucose/$id") }
                            )
                        }
                        composable("medication_sets") {
                            MedicationSetsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onAddSet = { navController.navigate("add_medication_set") },
                                onEditSet = { id -> navController.navigate("edit_medication_set/$id") }
                            )
                        }
                        composable("add_medication_set") {
                            AddMedicationSetScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = "edit_medication_set/{setId}",
                            arguments = listOf(navArgument("setId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val setId = backStackEntry.arguments?.getLong("setId")
                            AddMedicationSetScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                editId = setId
                            )
                        }
                        // Edit routes
                        composable(
                            route = "edit_meal/{mealId}",
                            arguments = listOf(navArgument("mealId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val mealId = backStackEntry.arguments?.getLong("mealId")
                            AddMealScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                editId = mealId
                            )
                        }
                        composable(
                            route = "edit_symptom/{symptomId}",
                            arguments = listOf(navArgument("symptomId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val symptomId = backStackEntry.arguments?.getLong("symptomId")
                            AddSymptomScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                editId = symptomId
                            )
                        }
                        composable(
                            route = "edit_other/{otherEntryId}",
                            arguments = listOf(navArgument("otherEntryId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val otherEntryId = backStackEntry.arguments?.getLong("otherEntryId")
                            AddOtherEntryScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                editId = otherEntryId
                            )
                        }
                        composable(
                            route = "edit_medication/{medicationId}",
                            arguments = listOf(navArgument("medicationId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val medicationId = backStackEntry.arguments?.getLong("medicationId")
                            AddMedicationScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                editId = medicationId
                            )
                        }
                        composable(
                            route = "edit_blood_pressure/{bloodPressureId}",
                            arguments = listOf(navArgument("bloodPressureId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val bloodPressureId = backStackEntry.arguments?.getLong("bloodPressureId")
                            AddBloodPressureScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                editId = bloodPressureId
                            )
                        }
                        composable(
                            route = "edit_cholesterol/{cholesterolId}",
                            arguments = listOf(navArgument("cholesterolId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val cholesterolId = backStackEntry.arguments?.getLong("cholesterolId")
                            AddCholesterolScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                editId = cholesterolId
                            )
                        }
                        composable(
                            route = "edit_weight/{weightId}",
                            arguments = listOf(navArgument("weightId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val weightId = backStackEntry.arguments?.getLong("weightId")
                            AddWeightScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                editId = weightId
                            )
                        }
                        composable(
                            route = "edit_spo2/{spo2Id}",
                            arguments = listOf(navArgument("spo2Id") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val spo2Id = backStackEntry.arguments?.getLong("spo2Id")
                            AddSpO2Screen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                editId = spo2Id
                            )
                        }
                        composable(
                            route = "edit_blood_glucose/{bloodGlucoseId}",
                            arguments = listOf(navArgument("bloodGlucoseId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val bloodGlucoseId = backStackEntry.arguments?.getLong("bloodGlucoseId")
                            AddBloodGlucoseScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                editId = bloodGlucoseId
                            )
                        }
                    }
                }
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            ReminderBroadcastReceiver.CHANNEL_ID,
            "Medication Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminders to log medication sets"
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
