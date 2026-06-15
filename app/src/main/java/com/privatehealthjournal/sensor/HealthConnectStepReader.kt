package com.privatehealthjournal.sensor

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.privatehealthjournal.data.entity.StepSource
import com.privatehealthjournal.data.preferences.AppPreferences
import com.privatehealthjournal.data.preferences.StepPrefs
import com.privatehealthjournal.data.repository.StepsRepository
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class HealthConnectStepReader(
    private val context: Context,
    private val repository: StepsRepository
) {
    suspend fun sync() {
        if (!AppPreferences.getShowStepCounting(context).first()) return
        if (!AppPreferences.getHealthConnectEnabled(context).first()) return

        val sdkStatus = HealthConnectClient.getSdkStatus(context)
        if (sdkStatus != HealthConnectClient.SDK_AVAILABLE) {
            Log.d(TAG, "Health Connect SDK not available: $sdkStatus")
            return
        }

        val client = try {
            HealthConnectClient.getOrCreate(context)
        } catch (e: Exception) {
            Log.w(TAG, "HealthConnectClient.getOrCreate failed", e)
            return
        }

        val readPerm = HealthPermission.getReadPermission(StepsRecord::class)
        val granted = try {
            client.permissionController.getGrantedPermissions()
        } catch (e: Exception) {
            Log.w(TAG, "getGrantedPermissions failed", e)
            return
        }
        if (readPerm !in granted) return

        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()
        val startDay = today.minusDays(7)
        val startInstant = startDay.atStartOfDay(zone).toInstant()
        val endInstant = Instant.now()

        val records = try {
            client.readRecords(
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startInstant, endInstant)
                )
            ).records
        } catch (e: Exception) {
            Log.w(TAG, "readRecords failed", e)
            return
        }

        val byDay = mutableMapOf<Long, Long>()
        records.forEach { record ->
            val day = record.startTime.atZone(zone).toLocalDate().toEpochDay()
            byDay[day] = (byDay[day] ?: 0L) + record.count
        }

        byDay.forEach { (epochDay, totalSteps) ->
            repository.recordStepCount(epochDay, totalSteps.toInt(), StepSource.HEALTH_CONNECT)
        }
        StepPrefs.setHcLastSyncEpochDay(context, today.toEpochDay())
    }

    companion object {
        private const val TAG = "HCStepReader"

        val REQUIRED_PERMISSIONS = setOf(
            HealthPermission.getReadPermission(StepsRecord::class)
        )
    }
}
