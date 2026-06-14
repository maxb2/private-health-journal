package com.privatehealthjournal.sensor

import android.content.Context
import android.util.Log
import com.privatehealthjournal.data.repository.LogRepository

class StepSync(
    private val sensorReader: StepSensorReader,
    private val hcReader: HealthConnectStepReader
) {
    suspend fun syncOnResume() {
        try {
            hcReader.sync()
        } catch (e: Exception) {
            Log.w(TAG, "HC sync failed", e)
        }
        try {
            sensorReader.sync()
        } catch (e: Exception) {
            Log.w(TAG, "sensor sync failed", e)
        }
    }

    companion object {
        private const val TAG = "StepSync"

        fun create(context: Context, repository: LogRepository): StepSync {
            return StepSync(
                StepSensorReader(context.applicationContext, repository),
                HealthConnectStepReader(context.applicationContext, repository)
            )
        }
    }
}
