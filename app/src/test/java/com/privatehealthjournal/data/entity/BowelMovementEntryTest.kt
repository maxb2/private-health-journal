package com.privatehealthjournal.data.entity

import com.privatehealthjournal.util.TestData
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BowelMovementEntryTest {

    @Test
    fun `BristolType fromInt returns correct type for type 1`() {
        val result = BristolType.fromInt(1)
        assertThat(result).isEqualTo(BristolType.TYPE_1)
        assertThat(result.displayName).isEqualTo("Type 1")
    }

    @Test
    fun `BristolType fromInt returns correct type for type 4`() {
        val result = BristolType.fromInt(4)
        assertThat(result).isEqualTo(BristolType.TYPE_4)
        assertThat(result.displayName).isEqualTo("Type 4")
    }

    @Test
    fun `BristolType fromInt returns correct type for type 7`() {
        val result = BristolType.fromInt(7)
        assertThat(result).isEqualTo(BristolType.TYPE_7)
        assertThat(result.displayName).isEqualTo("Type 7")
    }

    @Test
    fun `BristolType fromInt works for all valid types`() {
        for (i in 1..7) {
            val result = BristolType.fromInt(i)
            assertThat(result.type).isEqualTo(i)
        }
    }

    @Test(expected = NoSuchElementException::class)
    fun `BristolType fromInt throws for invalid type 0`() {
        BristolType.fromInt(0)
    }

    @Test(expected = NoSuchElementException::class)
    fun `BristolType fromInt throws for invalid type 8`() {
        BristolType.fromInt(8)
    }

    @Test
    fun `BristolType has correct descriptions`() {
        assertThat(BristolType.TYPE_1.description).contains("hard lumps")
        assertThat(BristolType.TYPE_4.description).contains("smooth and soft")
        assertThat(BristolType.TYPE_7.description).contains("Watery")
    }

    @Test
    fun `BowelMovementEntry stores bristolType correctly`() {
        val entry = TestData.createBowelMovementEntry(bristolType = 3)
        assertThat(entry.bristolType).isEqualTo(3)
    }

    @Test
    fun `BowelMovementEntry with all fields populated`() {
        val entry = BowelMovementEntry(
            id = 5L,
            bristolType = 4,
            notes = "Normal",
            timestamp = 12345L
        )

        assertThat(entry.id).isEqualTo(5L)
        assertThat(entry.bristolType).isEqualTo(4)
        assertThat(entry.notes).isEqualTo("Normal")
        assertThat(entry.timestamp).isEqualTo(12345L)
    }

    @Test
    fun `BowelMovementEntry with default values`() {
        val entry = BowelMovementEntry(bristolType = 4)

        assertThat(entry.id).isEqualTo(0L)
        assertThat(entry.notes).isEmpty()
    }
}
