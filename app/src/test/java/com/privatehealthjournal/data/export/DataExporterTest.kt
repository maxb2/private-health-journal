package com.privatehealthjournal.data.export

import com.privatehealthjournal.data.entity.MealType
import com.privatehealthjournal.data.entity.OtherEntryType
import com.privatehealthjournal.util.TestData
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import org.junit.Test

class DataExporterTest {

    private val gson = Gson()

    @Test
    fun `export returns valid JSON`() {
        val json = DataExporter.export(
            meals = emptyList(),
            symptoms = emptyList(),
            medications = emptyList(),
            otherEntries = emptyList()
        )

        val result = gson.fromJson(json, ExportData::class.java)
        assertThat(result).isNotNull()
    }

    @Test
    fun `export includes version and exportedAt`() {
        val json = DataExporter.export(
            meals = emptyList(),
            symptoms = emptyList(),
            medications = emptyList(),
            otherEntries = emptyList()
        )

        val result = gson.fromJson(json, ExportData::class.java)
        assertThat(result.version).isEqualTo(3)
        assertThat(result.exportedAt).isGreaterThan(0L)
    }

    @Test
    fun `export empty data returns empty lists`() {
        val json = DataExporter.export(
            meals = emptyList(),
            symptoms = emptyList(),
            medications = emptyList(),
            otherEntries = emptyList()
        )

        val result = gson.fromJson(json, ExportData::class.java)
        assertThat(result.meals).isEmpty()
        assertThat(result.symptoms).isEmpty()
        assertThat(result.medications).isEmpty()
        assertThat(result.otherEntries).isEmpty()
    }

    @Test
    fun `export meals correctly`() {
        val meal = TestData.createMealEntry(
            mealType = MealType.LUNCH,
            notes = "Test meal notes",
            timestamp = 12345L
        )
        val foods = listOf(
            TestData.createFoodItem(name = "Pizza"),
            TestData.createFoodItem(id = 2L, name = "Salad")
        )
        val tags = listOf(
            TestData.createTag(name = "Italian"),
            TestData.createTag(id = 2L, name = "Quick")
        )
        val mealWithDetails = TestData.createMealWithDetails(
            meal = meal,
            foods = foods,
            tags = tags
        )

        val json = DataExporter.export(
            meals = listOf(mealWithDetails),
            symptoms = emptyList(),
            medications = emptyList(),
            otherEntries = emptyList()
        )

        val result = gson.fromJson(json, ExportData::class.java)
        assertThat(result.meals).hasSize(1)

        val exportedMeal = result.meals[0]
        assertThat(exportedMeal.mealType).isEqualTo("LUNCH")
        assertThat(exportedMeal.notes).isEqualTo("Test meal notes")
        assertThat(exportedMeal.timestamp).isEqualTo(12345L)
        assertThat(exportedMeal.foods).containsExactly("Pizza", "Salad")
        assertThat(exportedMeal.tags).containsExactly("Italian", "Quick")
    }

    @Test
    fun `export symptoms correctly`() {
        val symptom = TestData.createSymptomEntry(
            name = "Headache",
            severity = 4,
            notes = "After lunch",
            startTime = 1000L,
            endTime = 2000L
        )

        val json = DataExporter.export(
            meals = emptyList(),
            symptoms = listOf(symptom),
            medications = emptyList(),
            otherEntries = emptyList()
        )

        val result = gson.fromJson(json, ExportData::class.java)
        assertThat(result.symptoms).hasSize(1)

        val exportedSymptom = result.symptoms[0]
        assertThat(exportedSymptom.name).isEqualTo("Headache")
        assertThat(exportedSymptom.severity).isEqualTo(4)
        assertThat(exportedSymptom.notes).isEqualTo("After lunch")
        assertThat(exportedSymptom.startTime).isEqualTo(1000L)
        assertThat(exportedSymptom.endTime).isEqualTo(2000L)
    }

    @Test
    fun `export ongoing symptom with null endTime`() {
        val symptom = TestData.createSymptomEntry(
            name = "Fatigue",
            severity = 2,
            endTime = null
        )

        val json = DataExporter.export(
            meals = emptyList(),
            symptoms = listOf(symptom),
            medications = emptyList(),
            otherEntries = emptyList()
        )

        val result = gson.fromJson(json, ExportData::class.java)
        assertThat(result.symptoms[0].endTime).isNull()
    }

    @Test
    fun `export medications correctly`() {
        val medication = TestData.createMedicationEntry(
            name = "Aspirin",
            dosage = "500mg",
            notes = "For headache",
            timestamp = 3000L
        )

        val json = DataExporter.export(
            meals = emptyList(),
            symptoms = emptyList(),
            medications = listOf(medication),
            otherEntries = emptyList()
        )

        val result = gson.fromJson(json, ExportData::class.java)
        assertThat(result.medications).hasSize(1)

        val exportedMedication = result.medications[0]
        assertThat(exportedMedication.name).isEqualTo("Aspirin")
        assertThat(exportedMedication.dosage).isEqualTo("500mg")
        assertThat(exportedMedication.notes).isEqualTo("For headache")
        assertThat(exportedMedication.timestamp).isEqualTo(3000L)
    }

    @Test
    fun `export other entries correctly`() {
        val otherEntry = TestData.createOtherEntry(
            entryType = OtherEntryType.SLEEP,
            subType = "Night sleep",
            value = "8 hours",
            notes = "Good sleep",
            timestamp = 4000L
        )

        val json = DataExporter.export(
            meals = emptyList(),
            symptoms = emptyList(),
            medications = emptyList(),
            otherEntries = listOf(otherEntry)
        )

        val result = gson.fromJson(json, ExportData::class.java)
        assertThat(result.otherEntries).hasSize(1)

        val exportedEntry = result.otherEntries[0]
        assertThat(exportedEntry.entryType).isEqualTo("SLEEP")
        assertThat(exportedEntry.subType).isEqualTo("Night sleep")
        assertThat(exportedEntry.value).isEqualTo("8 hours")
        assertThat(exportedEntry.notes).isEqualTo("Good sleep")
        assertThat(exportedEntry.timestamp).isEqualTo(4000L)
    }

    @Test
    fun `export multiple entries of all types`() {
        val meal1 = TestData.createMealWithDetails(
            meal = TestData.createMealEntry(id = 1L, mealType = MealType.BREAKFAST)
        )
        val meal2 = TestData.createMealWithDetails(
            meal = TestData.createMealEntry(id = 2L, mealType = MealType.DINNER)
        )
        val symptom1 = TestData.createSymptomEntry(id = 1L, name = "Headache")
        val symptom2 = TestData.createSymptomEntry(id = 2L, name = "Nausea")
        val medication = TestData.createMedicationEntry()
        val otherEntry = TestData.createOtherEntry()

        val json = DataExporter.export(
            meals = listOf(meal1, meal2),
            symptoms = listOf(symptom1, symptom2),
            medications = listOf(medication),
            otherEntries = listOf(otherEntry)
        )

        val result = gson.fromJson(json, ExportData::class.java)
        assertThat(result.meals).hasSize(2)
        assertThat(result.symptoms).hasSize(2)
        assertThat(result.medications).hasSize(1)
        assertThat(result.otherEntries).hasSize(1)
    }

    @Test
    fun `export produces pretty printed JSON`() {
        val json = DataExporter.export(
            meals = emptyList(),
            symptoms = emptyList(),
            medications = emptyList(),
            otherEntries = emptyList()
        )

        // Pretty printed JSON should contain newlines
        assertThat(json).contains("\n")
    }
}
