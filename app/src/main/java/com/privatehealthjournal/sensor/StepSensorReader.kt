package com.privatehealthjournal.sensor

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.privatehealthjournal.data.entity.StepSource
import com.privatehealthjournal.data.preferences.AppPreferences
import com.privatehealthjournal.data.preferences.StepPrefs
import com.privatehealthjournal.data.repository.LogRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.time.LocalDate
import kotlin.coroutines.resume

class StepSensorReader(
    private val context: Context,
    private val repository: LogRepository
) {
    suspend fun sync() {
        if (!AppPreferences.getShowStepCounting(context).first()) return
        if (!AppPreferences.getStepSensorEnabled(context).first()) return
        if (context.checkSelfPermission(android.Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager ?: return
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) ?: return

        val current = readOnce(sensorManager, sensor) ?: return
        val today = LocalDate.now().toEpochDay()
        val lastCumulative = StepPrefs.getLastCumulative(context)
        val lastEpochDay = StepPrefs.getLastReadingEpochDay(context)

        if (lastCumulative == null) {
            StepPrefs.setLastReading(context, current, today)
            return
        }

        val delta = if (current < lastCumulative) {
            current.toInt()
        } else {
            (current - lastCumulative).toInt()
        }

        if (delta > 0) {
            val existingToday = repository.getStepCountByEpochDay(today)
            val newTotal = if (lastEpochDay == today && existingToday != null && existingToday.source == StepSource.SENSOR) {
                existingToday.steps + delta
            } else {
                delta + (existingToday?.steps?.takeIf { existingToday.source == StepSource.SENSOR } ?: 0)
            }
            repository.recordStepCount(today, newTotal, StepSource.SENSOR)
        }
        StepPrefs.setLastReading(context, current, today)
    }

    private suspend fun readOnce(sensorManager: SensorManager, sensor: Sensor): Long? =
        withTimeoutOrNull(3000L) {
            suspendCancellableCoroutine<Long?> { cont ->
                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        val value = event.values.firstOrNull()?.toLong()
                        sensorManager.unregisterListener(this)
                        if (cont.isActive) cont.resume(value)
                    }
                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                }
                val registered = sensorManager.registerListener(
                    listener, sensor, SensorManager.SENSOR_DELAY_NORMAL
                )
                if (!registered) {
                    if (cont.isActive) cont.resume(null)
                }
                cont.invokeOnCancellation { sensorManager.unregisterListener(listener) }
            }
        }.also {
            if (it == null) Log.d(TAG, "step sensor read timed out / failed")
        }

    companion object {
        private const val TAG = "StepSensorReader"
    }
}
