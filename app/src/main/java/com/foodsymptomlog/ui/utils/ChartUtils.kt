package com.foodsymptomlog.ui.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class TimeRange(val label: String, val days: Int?) {
    SEVEN_DAYS("7 Days", 7),
    THIRTY_DAYS("30 Days", 30),
    NINETY_DAYS("90 Days", 90),
    ALL_TIME("All Time", null)
}

fun <T> filterByTimeRange(
    entries: List<T>,
    timeRange: TimeRange,
    timestampSelector: (T) -> Long
): List<T> {
    if (timeRange.days == null) return entries

    val cutoff = System.currentTimeMillis() - (timeRange.days * 24 * 60 * 60 * 1000L)
    return entries.filter { timestampSelector(it) >= cutoff }
}

fun formatDateForAxis(timestamp: Long, compact: Boolean = true): String {
    val date = Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

    return if (compact) {
        date.format(DateTimeFormatter.ofPattern("M/d/yy"))
    } else {
        date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    }
}

fun formatDateTimeForTooltip(timestamp: Long): String {
    val dateTime = Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
    return dateTime.format(DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a"))
}
