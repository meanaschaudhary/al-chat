# Al-Chat: Android Studio & APK/AAB Build Instructions

This guide provides beginner-friendly instructions to export this project and generate ready-to-install Android APKs or production Google Play App Bundles (AABs).

---

## 1. Opening the Project in Android Studio

1. Download the exported ZIP archive from Google AI Studio.
2. Extract the ZIP folder to a convenient location (e.g., `~/Documents/AlChat/`).
3. Open **Android Studio** (Hedgehog, Iguana, Jellyfish, or newer).
4. Click **Open** (or `File` → `Open`) and select the extracted folder root.
5. Android Studio will automatically synchronize the Gradle build files.

---

## 2. Running on Device / Emulator

1. In Android Studio toolbar, select your connected Android device or create a Virtual Device (AVD).
2. Click the green **Run** (▶) button or press `Shift + F10`.

---

## 3. Building a Debug APK (Instant Testing)

To generate an APK file you can immediately transfer and install on any Android phone:

### Via Android Studio Menu:
1. In the top menu bar, click **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**.
2. When the build completes, click **locate** in the notification balloon.
3. The APK is created at:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

### Via Terminal:
```bash
./gradlew assembleDebug
```

---

## 4. Generating a Signed Release APK / AAB (for Google Play Store)

To publish on Google Play or distribute a production signed build:

1. In Android Studio, go to **Build** → **Generate Signed Bundle / APK...**
2. Choose:
   - **Android App Bundle (.aab)** for Google Play Console submission.
   - **APK (.apk)** for direct sideloading release.
3. Click **Next**.
4. Click **Create new...** under Key store path to generate your secure upload keystore:
   - **Key store path**: Choose a safe location (e.g., `~/alchat-keystore.jks`).
   - **Password**: Enter a secure password (keep this safe!).
   - **Key Alias**: `alchat_key`
   - **Validity**: `25` years
   - Enter your Certificate details (First/Last Name, Org).
5. Click **OK**, then select **release** build variant.
6. Click **Create** / **Finish**.
7. Android Studio will generate the signed `.aab` or `.apk` in `app/release/`.

---

## 5. Summary of Architecture
- **Language**: Kotlin 2.0+
- **UI Framework**: Jetpack Compose & Material 3
- **Navigation**: Jetpack Navigation Compose
- **Image Caching**: Coil 2.6.0
- **Asynchronous Flow**: Kotlin Coroutines & StateFlow
- **Branding**: Original Al-Chat Monogram & Cyan/Teal Palette
