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
    val SHOW_STEP_COUNTING = booleanPreferencesKey("show_step_counting")
    val STEP_SENSOR_ENABLED = booleanPreferencesKey("step_sensor_enabled")
    val HEALTH_CONNECT_ENABLED = booleanPreferencesKey("health_connect_enabled")

    fun getShowCycleTracking(context: Context): Flow<Boolean> =
        context.appDataStore.data.map { prefs -> prefs[SHOW_CYCLE_TRACKING] ?: true }

    suspend fun setShowCycleTracking(context: Context, show: Boolean) {
        context.appDataStore.edit { prefs -> prefs[SHOW_CYCLE_TRACKING] = show }
    }

    fun getShowStepCounting(context: Context): Flow<Boolean> =
        context.appDataStore.data.map { prefs -> prefs[SHOW_STEP_COUNTING] ?: false }

    suspend fun setShowStepCounting(context: Context, show: Boolean) {
        context.appDataStore.edit { prefs -> prefs[SHOW_STEP_COUNTING] = show }
    }

    fun getStepSensorEnabled(context: Context): Flow<Boolean> =
        context.appDataStore.data.map { prefs -> prefs[STEP_SENSOR_ENABLED] ?: false }

    suspend fun setStepSensorEnabled(context: Context, enabled: Boolean) {
        context.appDataStore.edit { prefs -> prefs[STEP_SENSOR_ENABLED] = enabled }
    }

    fun getHealthConnectEnabled(context: Context): Flow<Boolean> =
        context.appDataStore.data.map { prefs -> prefs[HEALTH_CONNECT_ENABLED] ?: false }

    suspend fun setHealthConnectEnabled(context: Context, enabled: Boolean) {
        context.appDataStore.edit { prefs -> prefs[HEALTH_CONNECT_ENABLED] = enabled }
    }
}
