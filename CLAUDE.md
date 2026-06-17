# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build the app
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run tests
./gradlew test

# Run instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest

# Clean build
./gradlew clean

# Run app in emulator
adb install -r app/build/outputs/apk/debug/app-debug.apk && adb shell am start -n com.privatehealthjournal/.MainActivity 2>&1
```

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose with Material Design 3
- **Database:** Room with KSP for code generation
- **Architecture:** MVVM (per-feature ViewModel + Repository)
- **Navigation:** Jetpack Navigation Compose
- **DI:** Manual `AppContainer` + `AppViewModelFactory` (no Hilt/Koin — AGP 9.x compat)
- **Min SDK:** 26, Target SDK: 36

## Architecture

### Data Flow
```
UI Screens → <Feature>ViewModel → <Feature>Repository → Room DAOs → SQLite Database
              ↑                                          ↓
           StateFlow                                   Flow<List<T>>
```

Five feature ViewModels + their Repositories:

| Feature VM/Repo | Entities |
|---|---|
| **JournalViewModel** / **JournalRepository** | MealEntry (+ foods, tags), SymptomEntry, BowelMovementEntry, OtherEntry, CycleEntry. Also owns `dailyBudget` + `showCycleTracking` prefs. |
| **MedicationViewModel** / **MedicationRepository** | MedicationEntry, MedicationSet (+ items), MedicationSetReminder, MedicationSetLog. Includes atomic `logMedicationSetAtomically` (uses `TransactionRunner`) and `hasSetBeenLoggedToday`. Schedules reminders via `ReminderScheduler`. |
| **BiometricsViewModel** / **BiometricsRepository** | BloodPressureEntry, CholesterolEntry, WeightEntry, SpO2Entry, BloodGlucoseEntry. |
| **StepsViewModel** / **StepsRepository** | StepCountEntry. Includes `recordStepCount` priority merge (MANUAL > HEALTH_CONNECT > SENSOR). Owns step-related prefs. |
| **SettingsViewModel** | Cross-domain export/import only. Takes all 4 repos + `ReminderScheduler`. |

Composition root: `App.kt` (Application) holds an `AppContainer` (in `di/`) which lazy-instantiates repos. `AppViewModelFactory` constructs each VM from container repos. `MainActivity` acquires VMs via `viewModel(factory = factory)`.

### Key Entry Types
- **MealEntry** - Meals with type (breakfast/lunch/dinner/snack), foods, tags
- **SymptomEntry** - Symptoms with name, severity (1-5), startTime, endTime (nullable for ongoing)
- **BowelMovementEntry** - Bristol scale type (1-7)
- **MedicationEntry** - Medication name, dosage
- **OtherEntry** - Generic entries with typed variants: bowel movement, sleep, exercise, stress, mood, water intake, other. Mood stores a numeric level (1-10) in `value` and a description in `subType` with autocomplete from previously entered values.
- **MedicationSet** - Named group of medications (`MedicationSetItem`s) for batch logging. Logging a set creates a `MedicationSetLog` and individual `MedicationEntry` records.
- **MedicationSetReminder** - Per-set scheduled reminders with `hour`, `minute`, `daysOfWeek` (bitmask), `enabled`. Fires notifications via `AlarmManager`; suppressed if the set was already logged today.
- **Biometrics** - BloodPressureEntry, CholesterolEntry, WeightEntry, SpO2Entry, BloodGlucoseEntry
- **StepCountEntry** - Daily step total with source (MANUAL/HEALTH_CONNECT/SENSOR), `dateEpochDay` unique index.
- **CycleEntry** - Period tracking entry with flow intensity + symptoms.

### Cross-domain screens
HomeScreen, HistoryScreen, CalendarScreen, MealBudgetScreen, BiometricsChartScreen take multiple VMs as explicit parameters (no aggregator VM). SettingsScreen takes the relevant four (Settings + Journal + Medication + Steps).

### Navigation
Routes defined in `MainActivity.kt` using string-based navigation:
- `home`, `history`, `calendar`, `settings`
- `add_meal`, `add_symptom`, `add_medication`, `add_other`, `add_other?type={TYPE}`
- `edit_meal/{id}`, `edit_symptom/{id}`, etc.
- `medication_sets` (manage sets and reminders)

### Database
Room database (`AppDatabase.kt`) with hand-written migrations 8→15. `fallbackToDestructiveMigration()` is still set as a safety net — remove before shipping. Entity changes require version bump + a `Migration` object.

### Color Coding Convention
Entry cards use Material 3 container colors to distinguish types:
- Meals: `primaryContainer`
- Symptoms: `secondaryContainer`
- Bowel Movements: `tertiaryContainer`
- Biometrics + Steps: `surfaceVariant`

### Notifications & Reminders
Medication set reminders use `AlarmManager.setExactAndAllowWhileIdle()` scheduled via `notification/ReminderScheduler.kt`. Alarms are received by `notification/ReminderBroadcastReceiver.kt`, which also reschedules all reminders on `BOOT_COMPLETED`. Notification channel `"medication_reminders"` is created in `MainActivity.onCreate()`. Days-of-week use a bitmask (`DaysOfWeek` object in `MedicationSetReminder.kt`).

Manifest permissions: `USE_EXACT_ALARM`, `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`, `ACTIVITY_RECOGNITION`.

### Export/Import
JSON export/import in `data/export/`. `SettingsViewModel` collects snapshots from all 4 repos and calls `DataExporter.export(...)`. `DataImporter.import(json, ImportTarget)` routes inserts to the correct repo via the `ImportTarget` container.

## Adding a New Biometric Entry Type

Follow this checklist when adding a new biometric (like Blood Pressure, SpO2, Blood Glucose, etc.). Use any existing biometric as a reference — SpO2 is the simplest, Weight has a unit enum, Blood Glucose has both a unit enum and an optional context enum.

### Files to create (4)

1. **`data/entity/{Name}Entry.kt`** — Room `@Entity` data class with `id` (auto-generated PK), value fields, `notes: String = ""`, `timestamp: Long = System.currentTimeMillis()`. Add enums for units or categories in the same file.
2. **`data/dao/{Name}Dao.kt`** — `@Dao` interface with: `getAllXEntries(): Flow<List<X>>`, `getRecentXEntries(limit: Int): Flow<List<X>>`, `getById(id: Long): suspend X?`, `insert()`, `update()`, `delete()`, `deleteById()`.
3. **`ui/components/{Name}Card.kt`** — Composable card with icon, formatted value, optional sub-fields, notes, timestamp, edit/delete buttons. Uses `surfaceVariant` container color.
4. **`ui/screens/Add{Name}Screen.kt`** — Add/edit screen. Takes `viewModel: BiometricsViewModel`. Supports `editId: Long?` for edit mode via `LaunchedEffect` → `viewModel.loadXForEditing()`.

### Files to modify (8)

5. **`data/AppDatabase.kt`** — Add entity to `@Database(entities = [...])`, increment version, add `abstract fun xDao(): XDao`, add a `Migration` object.
6. **`data/repository/BiometricsRepository.kt`** — Add DAO constructor param, `allXEntries` Flow property, `getRecentXEntries()`, and CRUD suspend funs.
7. **`viewmodel/BiometricsViewModel.kt`** — Add `allXEntries` + `recentXEntries` StateFlows, `_editingX` MutableStateFlow, `addX()` / `updateX()` / `deleteX()` / `loadXForEditing()`, extend `clearEditingState()`.
8. **`di/AppContainer.kt`** — Add the new DAO to the `BiometricsRepository` constructor call.
9. **`MainActivity.kt`** — Import the screen, add `composable("add_x")` and `composable("edit_x/{xId}")` routes wired to `biometricsViewModel`.
10. **`ui/screens/HomeScreen.kt` / `HistoryScreen.kt` / `CalendarScreen.kt`** — Collect `recentXEntries` / `allXEntries` from `biometricsViewModel`, add to the screen's `EntryItem` / `HistoryEntry` / `CalendarEntry` sealed class, add card rendering, add nav menu item under Biometrics (Home only).
11. **`ui/screens/BiometricsChartScreen.kt` + `ui/components/BiometricsCharts.kt`** — Add enum value to `BiometricTab`, collect/filter, add `XChart()` and `XSummaryCard()` composables.
12. **`data/export/ExportData.kt` + `DataExporter.kt` + `DataImporter.kt`** — Add `ExportedX` data class + `xEntries` field, map in `DataExporter`, parse + insert via `target.biometrics.insertX(...)` in `DataImporter`, add counter to `ImportResult.Success`.

### Key patterns
- All entry lists use `Flow<List<T>>` from DAO → repository, converted to `StateFlow` via `.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())` in ViewModel.
- Edit mode: screen takes `editId: Long?`, calls `viewModel.loadXForEditing(editId)` in `LaunchedEffect`, populates form from `viewModel.editingX` StateFlow.
- Biometric cards all use `surfaceVariant` container color (not the entry-type color coding used by meals/symptoms/bowel movements).
- Charts use the Vico charting library (`ChartEntryModelProducer`, `LineChart.LineSpec`, day-offset x-axis).
- Export enums as `.name` strings, import with `try/catch { valueOf() }` fallback to a sensible default.
