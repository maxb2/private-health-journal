package com.privatehealthjournal.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.privatehealthjournal.data.export.DataExporter
import com.privatehealthjournal.data.export.DataImporter
import com.privatehealthjournal.data.export.ImportResult
import com.privatehealthjournal.data.export.ImportTarget
import com.privatehealthjournal.data.repository.BiometricsRepository
import com.privatehealthjournal.data.repository.JournalRepository
import com.privatehealthjournal.data.repository.MedicationRepository
import com.privatehealthjournal.data.repository.StepsRepository
import com.privatehealthjournal.di.TransactionRunner
import com.privatehealthjournal.notification.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(
    private val journalRepository: JournalRepository,
    private val medicationRepository: MedicationRepository,
    private val biometricsRepository: BiometricsRepository,
    private val stepsRepository: StepsRepository,
    private val tx: TransactionRunner,
    application: Application,
) : AndroidViewModel(application) {

    private data class ExportSnapshot(
        val meals: List<com.privatehealthjournal.data.entity.MealWithDetails>,
        val symptoms: List<com.privatehealthjournal.data.entity.SymptomEntry>,
        val bowelMovements: List<com.privatehealthjournal.data.entity.BowelMovementEntry>,
        val otherEntries: List<com.privatehealthjournal.data.entity.OtherEntry>,
        val cycleEntries: List<com.privatehealthjournal.data.entity.CycleEntry>,
        val medications: List<com.privatehealthjournal.data.entity.MedicationEntry>,
        val medicationSets: List<com.privatehealthjournal.data.entity.MedicationSetWithItems>,
        val remindersBySetId: Map<Long, List<com.privatehealthjournal.data.entity.MedicationSetReminder>>,
        val logsBySetId: Map<Long, List<com.privatehealthjournal.data.entity.MedicationSetLog>>,
        val bloodPressure: List<com.privatehealthjournal.data.entity.BloodPressureEntry>,
        val cholesterol: List<com.privatehealthjournal.data.entity.CholesterolEntry>,
        val weight: List<com.privatehealthjournal.data.entity.WeightEntry>,
        val spO2: List<com.privatehealthjournal.data.entity.SpO2Entry>,
        val bloodGlucose: List<com.privatehealthjournal.data.entity.BloodGlucoseEntry>,
        val stepCountEntries: List<com.privatehealthjournal.data.entity.StepCountEntry>,
    )

    fun exportData(uri: Uri, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val snap = tx {
                    ExportSnapshot(
                        meals = journalRepository.allMeals.first(),
                        symptoms = journalRepository.allSymptomEntries.first(),
                        bowelMovements = journalRepository.allBowelMovements.first(),
                        otherEntries = journalRepository.allOtherEntries.first(),
                        cycleEntries = journalRepository.allCycleEntries.first(),
                        medications = medicationRepository.allMedications.first(),
                        medicationSets = medicationRepository.allMedicationSets.first(),
                        remindersBySetId = medicationRepository.getAllReminders().first().groupBy { it.setId },
                        logsBySetId = medicationRepository.getAllMedicationSetLogs().first().groupBy { it.setId },
                        bloodPressure = biometricsRepository.allBloodPressureEntries.first(),
                        cholesterol = biometricsRepository.allCholesterolEntries.first(),
                        weight = biometricsRepository.allWeightEntries.first(),
                        spO2 = biometricsRepository.allSpO2Entries.first(),
                        bloodGlucose = biometricsRepository.allBloodGlucoseEntries.first(),
                        stepCountEntries = stepsRepository.allStepCountEntries.first(),
                    )
                }
                val meals = snap.meals
                val symptoms = snap.symptoms
                val bowelMovements = snap.bowelMovements
                val otherEntries = snap.otherEntries
                val cycleEntries = snap.cycleEntries
                val medications = snap.medications
                val medicationSets = snap.medicationSets
                val remindersBySetId = snap.remindersBySetId
                val logsBySetId = snap.logsBySetId
                val bloodPressure = snap.bloodPressure
                val cholesterol = snap.cholesterol
                val weight = snap.weight
                val spO2 = snap.spO2
                val bloodGlucose = snap.bloodGlucose
                val stepCountEntries = snap.stepCountEntries

                val json = withContext(Dispatchers.IO) {
                    DataExporter.export(
                        meals = meals,
                        symptoms = symptoms,
                        bowelMovements = bowelMovements,
                        medications = medications,
                        otherEntries = otherEntries,
                        bloodPressureEntries = bloodPressure,
                        cholesterolEntries = cholesterol,
                        weightEntries = weight,
                        spO2Entries = spO2,
                        bloodGlucoseEntries = bloodGlucose,
                        medicationSets = medicationSets,
                        remindersBySetId = remindersBySetId,
                        logsBySetId = logsBySetId,
                        cycleEntries = cycleEntries,
                        stepCountEntries = stepCountEntries,
                    )
                }
                withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver.openOutputStream(uri)
                        ?.use { stream -> stream.write(json.toByteArray()) }
                        ?: throw IllegalStateException("Could not open output stream")
                }
                val total = meals.size + symptoms.size + bowelMovements.size +
                    medications.size + otherEntries.size +
                    bloodPressure.size + cholesterol.size + weight.size + spO2.size +
                    bloodGlucose.size + medicationSets.size +
                    cycleEntries.size + stepCountEntries.size
                onResult(true, "Exported $total entries successfully")
            } catch (e: Exception) {
                onResult(false, "Export failed: ${e.message}")
            }
        }
    }

    fun importData(uri: Uri, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val json = withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver.openInputStream(uri)?.use { stream ->
                        readJsonCapped(stream, MAX_IMPORT_BYTES)
                    }
                } ?: run {
                    onResult(false, "Could not read file")
                    return@launch
                }

                val result = applyImport(json)
                when (result) {
                    is ImportResult.Success -> {
                        val parts = buildList {
                            add("${result.mealsImported} meals")
                            add("${result.symptomsImported} symptoms")
                            add("${result.bowelMovementsImported} bowel movements")
                            add("${result.medicationsImported} medications")
                            add("${result.otherEntriesImported} other")
                            add("${result.bloodPressureImported} BP")
                            add("${result.cholesterolImported} cholesterol")
                            add("${result.weightImported} weight")
                            add("${result.spO2Imported} SpO2")
                            add("${result.bloodGlucoseImported} glucose")
                            add("${result.medicationSetsImported} med sets")
                            add("${result.cycleEntriesImported} cycle")
                            add("${result.stepCountEntriesImported} steps")
                        }
                        onResult(true, "Imported ${result.totalImported} entries: " + parts.joinToString(", "))
                    }
                    is ImportResult.Error -> {
                        onResult(false, result.message)
                    }
                }
            } catch (e: ImportTooLargeException) {
                onResult(false, "Import file too large (max ${MAX_IMPORT_BYTES / 1024 / 1024} MB)")
            } catch (e: Exception) {
                onResult(false, "Import failed: ${e.message}")
            }
        }
    }

    private class ImportTooLargeException : RuntimeException()
    private class ImportFailedException(message: String) : RuntimeException(message)

    /**
     * Apply [json] inside a single Room transaction. If [DataImporter] returns
     * Error, the exception escapes [tx] so partial inserts roll back, then we
     * convert back to ImportResult.Error for the UI.
     *
     * Also triggers reminder rescheduling when reminders were imported.
     * Exposed (internal) for unit tests — keeps the Uri/InputStream plumbing
     * out of the test path.
     */
    internal suspend fun applyImport(json: String): ImportResult {
        val target = ImportTarget(
            journal = journalRepository,
            medication = medicationRepository,
            biometrics = biometricsRepository,
            steps = stepsRepository,
        )
        val result: ImportResult = try {
            tx {
                when (val r = DataImporter.import(json, target)) {
                    is ImportResult.Success -> r
                    is ImportResult.Error -> throw ImportFailedException(r.message)
                }
            }
        } catch (e: ImportFailedException) {
            ImportResult.Error(e.message ?: "Import failed")
        }
        if (result is ImportResult.Success && result.medicationSetRemindersImported > 0) {
            ReminderScheduler.rescheduleAllReminders(getApplication())
        }
        return result
    }

    private fun readJsonCapped(stream: java.io.InputStream, max: Long): String {
        val out = java.io.ByteArrayOutputStream()
        val buf = ByteArray(8 * 1024)
        var total = 0L
        while (true) {
            val read = stream.read(buf)
            if (read == -1) break
            total += read
            if (total > max) throw ImportTooLargeException()
            out.write(buf, 0, read)
        }
        return out.toString(Charsets.UTF_8.name())
    }

    companion object {
        private const val MAX_IMPORT_BYTES: Long = 50L * 1024 * 1024
    }
}
