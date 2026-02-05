package com.foodsymptomlog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.foodsymptomlog.ui.screens.AddBowelMovementScreen
import com.foodsymptomlog.ui.screens.AddMealScreen
import com.foodsymptomlog.ui.screens.AddSymptomScreen
import com.foodsymptomlog.ui.screens.HistoryScreen
import com.foodsymptomlog.ui.screens.HomeScreen
import com.foodsymptomlog.ui.theme.FoodSymptomLogTheme
import com.foodsymptomlog.viewmodel.LogViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FoodSymptomLogTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val viewModel: LogViewModel = viewModel()

                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(
                                viewModel = viewModel,
                                onAddMeal = { navController.navigate("add_meal") },
                                onAddSymptom = { navController.navigate("add_symptom") },
                                onAddBowelMovement = { navController.navigate("add_bowel_movement") },
                                onViewHistory = { navController.navigate("history") },
                                onEditMeal = { id -> navController.navigate("edit_meal/$id") },
                                onEditSymptom = { id -> navController.navigate("edit_symptom/$id") },
                                onEditBowelMovement = { id -> navController.navigate("edit_bowel_movement/$id") }
                            )
                        }
                        composable("add_meal") {
                            AddMealScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("add_symptom") {
                            AddSymptomScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("add_bowel_movement") {
                            AddBowelMovementScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("history") {
                            HistoryScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onEditMeal = { id -> navController.navigate("edit_meal/$id") },
                                onEditSymptom = { id -> navController.navigate("edit_symptom/$id") },
                                onEditBowelMovement = { id -> navController.navigate("edit_bowel_movement/$id") }
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
                            route = "edit_bowel_movement/{bowelMovementId}",
                            arguments = listOf(navArgument("bowelMovementId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val bowelMovementId = backStackEntry.arguments?.getLong("bowelMovementId")
                            AddBowelMovementScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                editId = bowelMovementId
                            )
                        }
                    }
                }
            }
        }
    }
}
