package com.foodsymptomlog.data.entity

import com.foodsymptomlog.util.TestData
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SymptomEntryTest {

    @Test
    fun `isOngoing returns true when endTime is null`() {
        val symptom = TestData.createSymptomEntry(endTime = null)
        assertThat(symptom.isOngoing).isTrue()
    }

    @Test
    fun `isOngoing returns false when endTime is set`() {
        val symptom = TestData.createSymptomEntry(endTime = 2000L)
        assertThat(symptom.isOngoing).isFalse()
    }

    @Test
    fun `timestamp returns startTime for backwards compatibility`() {
        val startTime = 12345L
        val symptom = TestData.createSymptomEntry(startTime = startTime)
        assertThat(symptom.timestamp).isEqualTo(startTime)
    }

    @Test
    fun `severity values are within valid range`() {
        for (severity in 1..5) {
            val symptom = TestData.createSymptomEntry(severity = severity)
            assertThat(symptom.severity).isIn(1..5)
        }
    }

    @Test
    fun `symptom entry with all fields populated`() {
        val symptom = SymptomEntry(
            id = 10L,
            name = "Nausea",
            severity = 4,
            notes = "After lunch",
            startTime = 1000L,
            endTime = 2000L
        )

        assertThat(symptom.id).isEqualTo(10L)
        assertThat(symptom.name).isEqualTo("Nausea")
        assertThat(symptom.severity).isEqualTo(4)
        assertThat(symptom.notes).isEqualTo("After lunch")
        assertThat(symptom.startTime).isEqualTo(1000L)
        assertThat(symptom.endTime).isEqualTo(2000L)
        assertThat(symptom.isOngoing).isFalse()
    }

    @Test
    fun `symptom entry with default values`() {
        val symptom = SymptomEntry(name = "Test", severity = 1)

        assertThat(symptom.id).isEqualTo(0L)
        assertThat(symptom.notes).isEmpty()
        assertThat(symptom.endTime).isNull()
        assertThat(symptom.isOngoing).isTrue()
    }
}
