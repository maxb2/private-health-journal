package com.privatehealthjournal.ui.nav

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class NavIntentTest {

    @Test
    fun topLevel_routes() {
        assertEquals("history", NavIntent.History.route())
        assertEquals("calendar", NavIntent.Calendar.route())
        assertEquals("settings", NavIntent.Settings.route())
        assertEquals("medication_sets", NavIntent.MedicationSets.route())
        assertEquals("meal_budget", NavIntent.MealBudget.route())
        assertEquals("biometrics_chart", NavIntent.BiometricsChart.route())
        assertEquals("cycle_tracking", NavIntent.CycleTracking.route())
    }

    @Test
    fun addScreens_withoutPrefill_produceBaseRoute() {
        assertEquals("add_meal", NavIntent.AddMeal.route())
        assertEquals("add_symptom", NavIntent.AddSymptom().route())
        assertEquals("add_medication", NavIntent.AddMedication().route())
        assertEquals("add_blood_pressure", NavIntent.AddBloodPressure.route())
    }

    @Test
    fun addScreens_withPrefill_embedQueryArg() {
        assertEquals("add_symptom?name=Headache", NavIntent.AddSymptom("Headache").route())
        assertEquals("add_medication?name=Aspirin", NavIntent.AddMedication("Aspirin").route())
        assertEquals("add_other?type=SLEEP", NavIntent.AddOther("SLEEP").route())
    }

    @Test
    fun editScreens_embedId() {
        assertEquals("edit_meal/42", NavIntent.EditMeal(42).route())
        assertEquals("edit_symptom/7", NavIntent.EditSymptom(7).route())
        assertEquals("edit_step_count/1", NavIntent.EditStepCount(1).route())
        assertEquals("edit_cycle_entry/99", NavIntent.EditCycleEntry(99).route())
    }

    @Test
    fun back_isNotARoute() {
        assertThrows(IllegalStateException::class.java) { NavIntent.Back.route() }
    }
}
