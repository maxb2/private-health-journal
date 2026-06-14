package com.privatehealthjournal.ui.nav

/**
 * One-shot navigation request a top-level screen hands to the host so the
 * Composable signatures stay small. Each [NavIntent] is translated into a
 * route string (or a back-pop) by [NavIntent.routeOrBack].
 */
sealed class NavIntent {
    data object Back : NavIntent()

    // Top-level destinations
    data object History : NavIntent()
    data object Calendar : NavIntent()
    data object Settings : NavIntent()
    data object MedicationSets : NavIntent()
    data object MealBudget : NavIntent()
    data object BiometricsChart : NavIntent()
    data object CycleTracking : NavIntent()

    // Add screens — some carry a prefill argument
    data object AddMeal : NavIntent()
    data class AddSymptom(val prefillName: String? = null) : NavIntent()
    data class AddMedication(val prefillName: String? = null) : NavIntent()
    data class AddOther(val type: String) : NavIntent()
    data object AddBloodPressure : NavIntent()
    data object AddCholesterol : NavIntent()
    data object AddWeight : NavIntent()
    data object AddSpO2 : NavIntent()
    data object AddBloodGlucose : NavIntent()
    data object AddStepCount : NavIntent()
    data object AddCycleEntry : NavIntent()

    // Edit screens — all carry an entity id
    data class EditMeal(val id: Long) : NavIntent()
    data class EditSymptom(val id: Long) : NavIntent()
    data class EditOther(val id: Long) : NavIntent()
    data class EditMedication(val id: Long) : NavIntent()
    data class EditBloodPressure(val id: Long) : NavIntent()
    data class EditCholesterol(val id: Long) : NavIntent()
    data class EditWeight(val id: Long) : NavIntent()
    data class EditSpO2(val id: Long) : NavIntent()
    data class EditBloodGlucose(val id: Long) : NavIntent()
    data class EditStepCount(val id: Long) : NavIntent()
    data class EditCycleEntry(val id: Long) : NavIntent()
}

/** Maps a non-[NavIntent.Back] intent to its Navigation Compose route string. */
fun NavIntent.route(): String = when (this) {
    NavIntent.Back -> error("Back is not a route — handle separately via popBackStack().")
    NavIntent.History -> "history"
    NavIntent.Calendar -> "calendar"
    NavIntent.Settings -> "settings"
    NavIntent.MedicationSets -> "medication_sets"
    NavIntent.MealBudget -> "meal_budget"
    NavIntent.BiometricsChart -> "biometrics_chart"
    NavIntent.CycleTracking -> "cycle_tracking"
    NavIntent.AddMeal -> "add_meal"
    is NavIntent.AddSymptom -> if (prefillName != null) "add_symptom?name=$prefillName" else "add_symptom"
    is NavIntent.AddMedication -> if (prefillName != null) "add_medication?name=$prefillName" else "add_medication"
    is NavIntent.AddOther -> "add_other?type=$type"
    NavIntent.AddBloodPressure -> "add_blood_pressure"
    NavIntent.AddCholesterol -> "add_cholesterol"
    NavIntent.AddWeight -> "add_weight"
    NavIntent.AddSpO2 -> "add_spo2"
    NavIntent.AddBloodGlucose -> "add_blood_glucose"
    NavIntent.AddStepCount -> "add_step_count"
    NavIntent.AddCycleEntry -> "add_cycle_entry"
    is NavIntent.EditMeal -> "edit_meal/$id"
    is NavIntent.EditSymptom -> "edit_symptom/$id"
    is NavIntent.EditOther -> "edit_other/$id"
    is NavIntent.EditMedication -> "edit_medication/$id"
    is NavIntent.EditBloodPressure -> "edit_blood_pressure/$id"
    is NavIntent.EditCholesterol -> "edit_cholesterol/$id"
    is NavIntent.EditWeight -> "edit_weight/$id"
    is NavIntent.EditSpO2 -> "edit_spo2/$id"
    is NavIntent.EditBloodGlucose -> "edit_blood_glucose/$id"
    is NavIntent.EditStepCount -> "edit_step_count/$id"
    is NavIntent.EditCycleEntry -> "edit_cycle_entry/$id"
}
