package com.privatehealthjournal.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

object AppPreferences {
    val SHOW_CYCLE_TRACKING = booleanPreferencesKey("show_cycle_tracking")

    fun getShowCycleTracking(context: Context): Flow<Boolean> =
        context.appDataStore.data.map { prefs -> prefs[SHOW_CYCLE_TRACKING] ?: true }

    suspend fun setShowCycleTracking(context: Context, show: Boolean) {
        context.appDataStore.edit { prefs -> prefs[SHOW_CYCLE_TRACKING] = show }
    }
}
