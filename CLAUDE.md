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

### Navigation
Routes defined in `MainActivity.kt` using string-based navigation:
- `home`, `history`, `calendar`, `settings`
- `add_meal`, `add_symptom`, `add_medication`, `add_other`
- `edit_meal/{id}`, `edit_symptom/{id}`, etc.

### Database
Room database at version 6 using `fallbackToDestructiveMigration()` (no migration support yet). Entity changes require version bump.

### Color Coding Convention
Entry cards use Material 3 container colors to distinguish types:
- Meals: `primaryContainer`
- Symptoms: `secondaryContainer`
- Bowel Movements: `tertiaryContainer`

### Export/Import
JSON export/import functionality in `data/export/` for backing up and restoring data.