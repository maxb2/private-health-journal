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
- **Architecture:** MVVM (ViewModel + Repository)
- **Navigation:** Jetpack Navigation Compose
- **Min SDK:** 26, Target SDK: 34

## Architecture

### Data Flow
```
UI Screens → LogViewModel → LogRepository → Room DAOs → SQLite Database
              ↑                              ↓
           StateFlow                      Flow<List<T>>
```

The `LogViewModel` holds all state as `StateFlow` objects and exposes CRUD methods. Screens collect these flows and call ViewModel methods for mutations.

### Key Entry Types
- **MealEntry** - Meals with type (breakfast/lunch/dinner/snack), foods, tags
- **SymptomEntry** - Symptoms with name, severity (1-5), startTime, endTime (nullable for ongoing)
- **BowelMovementEntry** - Bristol scale type (1-7)
- **MedicationEntry** - Medication name, dosage
- **OtherEntry** - Generic entries for anything else
- **Biometrics** - BloodPressureEntry, CholesterolEntry, WeightEntry, SpO2Entry, BloodGlucoseEntry

### Navigation
Routes defined in `MainActivity.kt` using string-based navigation:
- `home`, `history`, `calendar`, `settings`
- `add_meal`, `add_symptom`, `add_medication`, `add_other`
- `edit_meal/{id}`, `edit_symptom/{id}`, etc.

### Database
Room database at version 9 using `fallbackToDestructiveMigration()` (no migration support yet). Entity changes require version bump.

### Color Coding Convention
Entry cards use Material 3 container colors to distinguish types:
- Meals: `primaryContainer`
- Symptoms: `secondaryContainer`
- Bowel Movements: `tertiaryContainer`

### Export/Import
JSON export/import functionality in `data/export/` for backing up and restoring data.

## Adding a New Biometric Entry Type

Follow this checklist when adding a new biometric (like Blood Pressure, SpO2, Blood Glucose, etc.). Use any existing biometric as a reference — SpO2 is the simplest, Weight has a unit enum, Blood Glucose has both a unit enum and an optional context enum.

### Files to create (4)

1. **`data/entity/{Name}Entry.kt`** — Room `@Entity` data class with `id` (auto-generated PK), value fields, `notes: String = ""`, `timestamp: Long = System.currentTimeMillis()`. Add enums for units or categories in the same file.
2. **`data/dao/{Name}Dao.kt`** — `@Dao` interface with: `getAllXEntries(): Flow<List<X>>`, `getRecentXEntries(limit: Int): Flow<List<X>>`, `getById(id: Long): suspend X?`, `insert()`, `update()`, `delete()`, `deleteById()`.
3. **`ui/components/{Name}Card.kt`** — Composable card with icon, formatted value, optional sub-fields, notes, timestamp, edit/delete buttons. Uses `surfaceVariant` container color. Add a private color helper for range-based coloring if applicable.
4. **`ui/screens/Add{Name}Screen.kt`** — Add/edit screen with form fields, validation, `DateTimePicker`, notes. Supports `editId: Long?` parameter for edit mode via `LaunchedEffect` → `viewModel.loadXForEditing()`.

### Files to modify (12)

5. **`data/AppDatabase.kt`** — Add entity to `@Database(entities = [...])`, increment version, add `abstract fun xDao(): XDao`.
6. **`data/repository/LogRepository.kt`** — Add DAO constructor param, `allXEntries` Flow property, `getRecentXEntries()`, and CRUD suspend funs (`insertX`, `updateX`, `deleteX`, `getXById`).
7. **`viewmodel/LogViewModel.kt`** — Add `allXEntries` + `recentXEntries` StateFlows (init from repository), pass new DAO to repository constructor, add `addX()` / `updateX()` / `deleteX()` methods, add `_editingX` MutableStateFlow + `editingX` exposed StateFlow, add `loadXForEditing()`, add to `clearEditingState()`, add to `exportData()` and `importData()`.
8. **`MainActivity.kt`** — Import screen, add `onAddX`/`onEditX` callbacks to HomeScreen/CalendarScreen/HistoryScreen composables, add `composable("add_x")` and `composable("edit_x/{xId}")` routes.
9. **`ui/screens/HomeScreen.kt`** — Add `onAddX`/`onEditX` params, collect `recentXEntries`, add dropdown menu item under Biometrics, add to `EntryItem` sealed class, add to `combinedEntries` list, add to `allEmpty` check, add card rendering in `when` block.
10. **`ui/screens/HistoryScreen.kt`** — Add `onEditX` param, collect `allXEntries`, add to `HistoryEntry` sealed class, include in `FilterType.ALL` and `FilterType.BIOMETRICS` lists, add card rendering.
11. **`ui/screens/CalendarScreen.kt`** — Add `onEditX` param, collect `allXEntries`, add to `CalendarEntry` sealed class, add to `entriesByDate` mapping loop, add to `selectedEntries` sorting `when`, add card rendering.
12. **`ui/screens/BiometricsChartScreen.kt`** — Add enum value to `BiometricTab`, collect + filter entries, add chart + summary rendering in `when` block.
13. **`ui/components/BiometricsCharts.kt`** — Add `XChart()` composable (line chart using Vico library pattern) and `XSummaryCard()` composable (latest/min/max/avg using `SummaryCard` helper).
14. **`data/export/ExportData.kt`** — Add `ExportedX` data class, add `xEntries` field to `ExportData` (default `emptyList()`).
15. **`data/export/DataExporter.kt`** — Add parameter to `export()`, map entries to `ExportedX`.
16. **`data/export/DataImporter.kt`** — Add import counter, parse + insert loop (with `try/catch` for enum valueOf), add to `ImportResult.Success` fields and `totalImported`.

### Key patterns
- All entry lists use `Flow<List<T>>` from DAO → repository, converted to `StateFlow` via `.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())` in ViewModel.
- Edit mode: screen takes `editId: Long?`, calls `viewModel.loadXForEditing(editId)` in `LaunchedEffect`, populates form from `viewModel.editingX` StateFlow.
- Biometric cards all use `surfaceVariant` container color (not the entry-type color coding used by meals/symptoms/bowel movements).
- Charts use the Vico charting library (`ChartEntryModelProducer`, `LineChart.LineSpec`, day-offset x-axis).
- Export enums as `.name` strings, import with `try/catch { valueOf() }` fallback to a sensible default.