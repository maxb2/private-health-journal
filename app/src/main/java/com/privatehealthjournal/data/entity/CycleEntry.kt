package com.privatehealthjournal.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class FlowIntensity(val displayLabel: String) {
    SPOTTING("Spotting"),
    LIGHT("Light"),
    MEDIUM("Medium"),
    HEAVY("Heavy")
}

enum class CycleSymptom(val displayLabel: String) {
    CRAMPS("Cramps"),
    BLOATING("Bloating"),
    HEADACHE("Headache"),
    FATIGUE("Fatigue"),
    MOOD_SWINGS("Mood Swings"),
    TENDER_BREASTS("Tender Breasts"),
    ACNE("Acne"),
    BACK_PAIN("Back Pain");

    companion object {
        fun encode(symptoms: Set<CycleSymptom>): String = symptoms.joinToString(",") { it.name }
        fun decode(raw: String): Set<CycleSymptom> =
            if (raw.isBlank()) emptySet()
            else raw.split(",").mapNotNull { runCatching { valueOf(it.trim()) }.getOrNull() }.toSet()
    }
}

@Entity(tableName = "cycle_entries", indices = [Index("timestamp")])
data class CycleEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val flow: FlowIntensity = FlowIntensity.MEDIUM,
    val symptoms: String = "",
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
