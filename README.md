# GitHub Development Dashboard

Android application built with **Jetpack Compose**, **Kotlin**, and **Material 3** for tracking and visualizing GitHub project repository health, workflows, and developer metrics.

## 📱 Features & Highlights
- **Modern UI:** Jetpack Compose with Material Design 3 and Unidirectional Data Flow (UDF).
- **Offline First:** Room Database local caching with Retrofit GitHub API integration.
- **Automated Releases:** GitHub Actions manual release workflow generating installable Android APKs attached directly to GitHub Releases.
- **Graph Engineering SDLC:** Autonomous agentic pipeline covering Definition (Architect), Readiness Gate (Three Amigos), Implementation & TDD (Dev & Test), Code Review (PR Review), and Backlog Triage.

## 🛠️ Tech Stack
- **OS / Platform:** Android (compileSdk 36, minSdk 24, targetSdk 36)
- **Language:** Kotlin 2.2.10 (Java 17)
- **UI Framework:** Jetpack Compose BOM 2024.09.00
- **Data & Networking:** Room 2.7.0, Retrofit 2.12.0, OkHttp 4.10.0, Moshi 1.15.2
- **Testing:** JUnit 4, AndroidX Test, Robolectric 4.16.1, Roborazzi 1.59.0

## 🚀 Quick Start & Development

### Prerequisites
- JDK 17+
- Android SDK (API 36)

### Common Commands
```powershell
# GitHub Auth Token setup
. C:\Users\rogal\workspaces\Set-GhToken-Antares.ps1

# Run unit tests
.\gradlew.bat testDebugUnitTest --no-daemon

# Build debug APK
.\gradlew.bat assembleDebug --no-daemon

# Build release APK
.\gradlew.bat assembleRelease --no-daemon

# Summarize unit tests
powershell -File .\scripts\summarize-unit-tests.ps1

# Register local SDLC pipeline scheduled tasks
powershell -File .\scripts\local-pipeline\register-local-tasks.ps1
```

## 📦 Releases
APKs are built and published on GitHub Releases via [`.github/workflows/release.yml`](.github/workflows/release.yml).
To trigger a release manually:
```powershell
gh workflow run release.yml --repo AntaresAndBharani/gh-development-dashboard
```
Or trigger it directly from the **Actions** tab in GitHub.

## 📄 License
Internal repository under AntaresAndBharani.
