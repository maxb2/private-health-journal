package com.privatehealthjournal.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Calendar

@Entity(
    tableName = "medication_set_reminders",
    foreignKeys = [
        ForeignKey(
            entity = MedicationSet::class,
            parentColumns = ["id"],
            childColumns = ["setId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("setId")]
)
data class MedicationSetReminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val setId: Long,
    val hour: Int,
    val minute: Int,
    val daysOfWeek: Int = DaysOfWeek.EVERY_DAY,
    val enabled: Boolean = true
)

object DaysOfWeek {
    const val MONDAY = 1
    const val TUESDAY = 2
    const val WEDNESDAY = 4
    const val THURSDAY = 8
    const val FRIDAY = 16
    const val SATURDAY = 32
    const val SUNDAY = 64
    const val EVERY_DAY = 127

    private val ALL_DAYS = listOf(
        MONDAY to "Mon",
        TUESDAY to "Tue",
        WEDNESDAY to "Wed",
        THURSDAY to "Thu",
        FRIDAY to "Fri",
        SATURDAY to "Sat",
        SUNDAY to "Sun"
    )

    fun isDayEnabled(bitmask: Int, day: Int): Boolean = bitmask and day != 0

    fun toDisplayString(bitmask: Int): String {
        if (bitmask == EVERY_DAY) return "Every day"
        val days = ALL_DAYS.filter { isDayEnabled(bitmask, it.first) }.map { it.second }
        return days.joinToString(", ")
    }

    fun fromCalendarDayOfWeek(calendarDay: Int): Int = when (calendarDay) {
        Calendar.MONDAY -> MONDAY
        Calendar.TUESDAY -> TUESDAY
        Calendar.WEDNESDAY -> WEDNESDAY
        Calendar.THURSDAY -> THURSDAY
        Calendar.FRIDAY -> FRIDAY
        Calendar.SATURDAY -> SATURDAY
        Calendar.SUNDAY -> SUNDAY
        else -> 0
    }
}
