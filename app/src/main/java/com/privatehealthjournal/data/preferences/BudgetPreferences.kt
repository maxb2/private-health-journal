package com.privatehealthjournal.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.budgetDataStore: DataStore<Preferences> by preferencesDataStore(name = "budget")

object BudgetPreferences {
    val DAILY_BUDGET = intPreferencesKey("daily_budget")
    val STEPS_PER_POINT_CREDIT = intPreferencesKey("steps_per_point_credit")

    fun getDailyBudget(context: Context): Flow<Int?> =
        context.budgetDataStore.data.map { prefs -> prefs[DAILY_BUDGET] }

    suspend fun setDailyBudget(context: Context, budget: Int) {
        context.budgetDataStore.edit { prefs -> prefs[DAILY_BUDGET] = budget }
    }

    suspend fun clearDailyBudget(context: Context) {
        context.budgetDataStore.edit { prefs -> prefs.remove(DAILY_BUDGET) }
    }

    fun getStepsPerPointCredit(context: Context): Flow<Int?> =
        context.budgetDataStore.data.map { prefs -> prefs[STEPS_PER_POINT_CREDIT] }

    suspend fun setStepsPerPointCredit(context: Context, value: Int) {
        context.budgetDataStore.edit { prefs -> prefs[STEPS_PER_POINT_CREDIT] = value }
    }

    suspend fun clearStepsPerPointCredit(context: Context) {
        context.budgetDataStore.edit { prefs -> prefs.remove(STEPS_PER_POINT_CREDIT) }
    }
}
