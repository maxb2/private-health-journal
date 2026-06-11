---
name: run-emulator
description: Builds and runs the food-symptom-log app on the Android emulator. Use when the user says "run on emulator", "open in emulator", "launch app", or wants to test the app live.
---

# Run on Emulator

Builds the debug APK and launches it on the Android emulator.

## Steps

### 1. Check if emulator is already running

```bash
adb devices

If output shows a device (e.g. emulator-5554  device), skip to step 3.

2. Start the emulator (if not running)

nohup /opt/homebrew/share/android-commandlinetools/emulator/emulator -avd Pixel_6_API_34 > /tmp/emulator.log 2>&1 &
echo "PID: $!"

Then wait for boot — poll until sys.boot_completed is 1:

adb wait-for-device && adb shell getprop sys.boot_completed

Repeat the getprop check until it returns 1. Use sleep 3 between attempts. Timeout after ~90 seconds.

3. Build the debug APK

./gradlew assembleDebug

If build fails, report the error to the user and stop.

4. Install and launch

adb install -r app/build/outputs/apk/debug/app-debug.apk && adb shell am start -n com.privatehealthjournal/.MainActivity

5. Confirm

Report success: APK installed, app launched. If anything failed, show the relevant error output.
EOF
```