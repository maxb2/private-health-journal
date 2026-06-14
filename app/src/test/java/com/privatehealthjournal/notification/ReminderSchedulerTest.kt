package com.privatehealthjournal.notification

import com.privatehealthjournal.data.entity.DaysOfWeek
import com.privatehealthjournal.data.entity.MedicationSetReminder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class ReminderSchedulerTest {

    private fun millis(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long {
        // month is 1-based for readability; Calendar.MONTH is 0-based.
        val cal = Calendar.getInstance(TimeZone.getDefault())
        cal.clear()
        cal.set(year, month - 1, day, hour, minute, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun reminder(hour: Int, minute: Int, days: Int) =
        MedicationSetReminder(id = 1, setId = 1, hour = hour, minute = minute, daysOfWeek = days)

    @Test
    fun `every-day reminder, time later today, schedules today`() {
        // 2026-03-04 was a Wednesday.
        val now = millis(2026, 3, 4, 7, 0)
        val expected = millis(2026, 3, 4, 8, 30)
        val actual = ReminderScheduler.computeNextTriggerTime(
            reminder(hour = 8, minute = 30, days = DaysOfWeek.EVERY_DAY),
            now
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `every-day reminder, time already passed today, schedules tomorrow`() {
        val now = millis(2026, 3, 4, 9, 0)
        val expected = millis(2026, 3, 5, 8, 30)
        val actual = ReminderScheduler.computeNextTriggerTime(
            reminder(hour = 8, minute = 30, days = DaysOfWeek.EVERY_DAY),
            now
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `weekday-only reminder skips weekend`() {
        // Friday 2026-03-06 at 20:00, reminder Mon-Fri at 09:00 — next slot is Monday 2026-03-09.
        val weekdays = DaysOfWeek.MONDAY or DaysOfWeek.TUESDAY or DaysOfWeek.WEDNESDAY or
            DaysOfWeek.THURSDAY or DaysOfWeek.FRIDAY
        val now = millis(2026, 3, 6, 20, 0)
        val expected = millis(2026, 3, 9, 9, 0)
        val actual = ReminderScheduler.computeNextTriggerTime(
            reminder(hour = 9, minute = 0, days = weekdays),
            now
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `weekend-only reminder from Wednesday picks Saturday`() {
        val weekend = DaysOfWeek.SATURDAY or DaysOfWeek.SUNDAY
        val now = millis(2026, 3, 4, 10, 0) // Wednesday
        val expected = millis(2026, 3, 7, 10, 0) // Saturday
        val actual = ReminderScheduler.computeNextTriggerTime(
            reminder(hour = 10, minute = 0, days = weekend),
            now
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `same-day enabled but past time wraps to next enabled day`() {
        // Wednesday-only reminder, currently Wednesday past the trigger time → next Wednesday.
        val now = millis(2026, 3, 4, 12, 0) // Wednesday
        val expected = millis(2026, 3, 11, 8, 0) // Following Wednesday
        val actual = ReminderScheduler.computeNextTriggerTime(
            reminder(hour = 8, minute = 0, days = DaysOfWeek.WEDNESDAY),
            now
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `no days enabled falls back to tomorrow same time`() {
        val now = millis(2026, 3, 4, 12, 0)
        val expected = millis(2026, 3, 5, 8, 0)
        val actual = ReminderScheduler.computeNextTriggerTime(
            reminder(hour = 8, minute = 0, days = 0),
            now
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `every-day reminder always lands in the future`() {
        val now = millis(2026, 3, 4, 23, 59)
        val actual = ReminderScheduler.computeNextTriggerTime(
            reminder(hour = 0, minute = 0, days = DaysOfWeek.EVERY_DAY),
            now
        )
        assertTrue("trigger must be after now", actual > now)
    }
}
