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

    fun getDailyBudget(context: Context): Flow<Int?> =
        context.budgetDataStore.data.map { prefs -> prefs[DAILY_BUDGET] }

    suspend fun setDailyBudget(context: Context, budget: Int) {
        context.budgetDataStore.edit { prefs -> prefs[DAILY_BUDGET] = budget }
    }

    suspend fun clearDailyBudget(context: Context) {
        context.budgetDataStore.edit { prefs -> prefs.remove(DAILY_BUDGET) }
    }
}
