package com.privatehealthjournal.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

val Context.stepDataStore: DataStore<Preferences> by preferencesDataStore(name = "step_prefs")

object StepPrefs {
    private val LAST_CUMULATIVE_COUNT = longPreferencesKey("last_cumulative_count")
    private val LAST_READING_EPOCH_DAY = longPreferencesKey("last_reading_epoch_day")
    private val HC_LAST_SYNC_EPOCH_DAY = longPreferencesKey("hc_last_sync_epoch_day")

    suspend fun getLastCumulative(context: Context): Long? =
        context.stepDataStore.data.first()[LAST_CUMULATIVE_COUNT]

    suspend fun getLastReadingEpochDay(context: Context): Long? =
        context.stepDataStore.data.first()[LAST_READING_EPOCH_DAY]

    suspend fun setLastReading(context: Context, cumulative: Long, epochDay: Long) {
        context.stepDataStore.edit { prefs ->
            prefs[LAST_CUMULATIVE_COUNT] = cumulative
            prefs[LAST_READING_EPOCH_DAY] = epochDay
        }
    }

    suspend fun getHcLastSyncEpochDay(context: Context): Long? =
        context.stepDataStore.data.first()[HC_LAST_SYNC_EPOCH_DAY]

    suspend fun setHcLastSyncEpochDay(context: Context, epochDay: Long) {
        context.stepDataStore.edit { prefs -> prefs[HC_LAST_SYNC_EPOCH_DAY] = epochDay }
    }
}
