# Building Noor (F-Droid Edition)

This document provides instructions for compiling the F-Droid edition of Noor completely from source using command-line Gradle.

## Prerequisites

- **Java Development Kit (JDK):** OpenJDK 17 or OpenJDK 21
- **Android SDK:** Command-line tools with API Level 36 (`platforms;android-36`) and Build-Tools `36.0.0`
- **Gradle:** Wrapper or system Gradle 8.x+

## Pure FOSS Architecture Guarantee

Noor (F-Droid Edition) is 100% Free and Open Source Software (FOSS):
- **Zero Proprietary SDKs:** No Google Play Services, Firebase, Gemini API, closed-source trackers, analytics, or advertising SDKs.
- **Zero Cloud Tracking:** All astronomy and prayer time calculations, Hijri calendar conversions, Qibla compass magnetic calculations, and Prayer Mat camera recognition run strictly on-device.
- **Zero API Keys Required:** The build does not require any `.env` files, API keys, or proprietary configuration files.

## Command-Line Build Instructions

### 1. Build Debug APK
```bash
gradle :app:assembleDebug
```
The generated APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

### 2. Build Release APK / Bundle
```bash
gradle :app:assembleRelease
```
Or to build an Android App Bundle:
```bash
gradle :app:bundleRelease
```

### 3. Run Local Unit Tests
```bash
gradle :app:testDebugUnitTest
```

## Dependencies & FOSS Licenses

All dependencies used in Noor are strictly FOSS:
- **AndroidX & Jetpack Compose:** Apache License 2.0
- **Kotlin & Coroutines:** Apache License 2.0
- **Jetpack Room & DataStore:** Apache License 2.0
- **CameraX:** Apache License 2.0
- **Coil (Image Loading):** Apache License 2.0
- **Moshi / OkHttp / Retrofit:** Apache License 2.0
- **Robolectric & JUnit:** MIT / EPL 1.0
