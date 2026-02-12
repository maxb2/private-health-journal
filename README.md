# Private Health Journal

An Android app for tracking meals, symptoms, medications, and bowel movements to help identify food sensitivities and patterns.
Your privacy matters to us. Your data is stored on your device and never sent anywhere.

## Features

- **Meal Tracking** - Log breakfast, lunch, dinner, and snacks with individual food items and tags
- **Symptom Tracking** - Record symptoms with severity (1-5) and duration
- **Bowel Movement Tracking** - Log using the Bristol stool scale (1-7)
- **Medication Tracking** - Track medications with dosage
- **Other Entries** - Generic entries for anything else worth noting
- **Calendar View** - See all entries organized by date
- **History View** - Browse past entries chronologically
- **Export/Import** - Back up and restore your data as JSON

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose with Material Design 3
- **Database:** Room (SQLite)
- **Architecture:** MVVM
- **Min SDK:** 26 (Android 8.0)

## Building

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run tests
./gradlew test
```

The debug APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

## License

MIT
