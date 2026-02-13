package com.privatehealthjournal.notification

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.privatehealthjournal.data.AppDatabase
import com.privatehealthjournal.data.entity.DaysOfWeek
import com.privatehealthjournal.data.entity.MedicationSetReminder
import java.util.Calendar

object ReminderScheduler {

    private const val ACTION_REMINDER = "com.privatehealthjournal.MEDICATION_REMINDER"
    private const val EXTRA_REMINDER_ID = "reminder_id"

    fun scheduleReminder(context: Context, reminder: MedicationSetReminder) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            action = ACTION_REMINDER
            putExtra(EXTRA_REMINDER_ID, reminder.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = computeNextTriggerTime(reminder)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }

    fun cancelReminder(context: Context, reminderId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            action = ACTION_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    suspend fun rescheduleAllReminders(context: Context) {
        val db = AppDatabase.getDatabase(context)
        val reminders = db.medicationSetReminderDao().getAllEnabledReminders()
        reminders.forEach { reminder ->
            scheduleReminder(context, reminder)
        }
    }

    fun dismissNotification(context: Context, setId: Long) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(setId.toInt())
    }

    fun computeNextTriggerTime(reminder: MedicationSetReminder): Long {
        val now = Calendar.getInstance()
        val candidate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminder.hour)
            set(Calendar.MINUTE, reminder.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Try up to 8 days to find the next matching day
        for (i in 0..7) {
            if (i > 0) {
                candidate.add(Calendar.DAY_OF_YEAR, 1)
            }
            val dayOfWeek = DaysOfWeek.fromCalendarDayOfWeek(candidate.get(Calendar.DAY_OF_WEEK))
            if (DaysOfWeek.isDayEnabled(reminder.daysOfWeek, dayOfWeek)) {
                if (candidate.after(now)) {
                    return candidate.timeInMillis
                }
            }
        }
        // Fallback: schedule for tomorrow at the specified time
        candidate.timeInMillis = now.timeInMillis
        candidate.add(Calendar.DAY_OF_YEAR, 1)
        candidate.set(Calendar.HOUR_OF_DAY, reminder.hour)
        candidate.set(Calendar.MINUTE, reminder.minute)
        candidate.set(Calendar.SECOND, 0)
        candidate.set(Calendar.MILLISECOND, 0)
        return candidate.timeInMillis
    }
}
