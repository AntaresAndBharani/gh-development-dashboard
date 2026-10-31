# GitHub Development Dashboard

Android application built with **Jetpack Compose**, **Kotlin**, and **Material 3** for tracking and visualizing GitHub project repository health, workflows, architecture compliance, and developer metrics.

## 📱 Features & Highlights
- **Modern UI:** Jetpack Compose with Material Design 3 and Unidirectional Data Flow (UDF).
- **Architecture & Way of Working Compliance Tracking:** Tracks cross-repository adoption and version currency (`standardVersion: "1.0.0"`) of the standardized **Claude Code Review** workflows (`.github/workflows/claude.yml` and `.github/workflows/pr-review.yml`) across heterogeneous live repositories (`AntaresAndBharani/crosstrainingapp`, `AntaresAndBharani/retro-fighter`, `AntaresAndBharani/darwin-trader`, `AntaresAndBharani/gh-development-dashboard`, `AntaresAndBharani/kerberito`, and `repo-examples/quant-trading-examples`) via [`architecture-registry.json`](architecture-registry.json) and live GitHub Contents API verification.
- **Offline First:** Room Database local caching with Retrofit GitHub API integration.
- **Automated Releases & CI:** GitHub Actions CI verification ([`.github/workflows/ci.yml`](.github/workflows/ci.yml)), Claude Opus 5.5 PR Review & Auto-Fix ([`.github/workflows/pr-review.yml`](.github/workflows/pr-review.yml)), and manual release workflow generating installable Android APKs attached directly to GitHub Releases.
- **Graph Engineering SDLC:** Autonomous agentic pipeline covering Definition (Architect), Readiness Gate (Three Amigos), Implementation & TDD (Dev & Test), Code Review (PR Review), and Backlog Triage.

## 🏛️ Architecture Registry (`architecture-registry.json`)
To keep heterogeneous repositories aligned on shared `AntaresAndBharani` practices (starting with **Claude Code Review**), this repository stores the canonical [`architecture-registry.json`](architecture-registry.json) and bundles a mirrored copy in [`app/src/main/assets/architecture-registry.json`](app/src/main/assets/architecture-registry.json).

When updating the Claude Code Review standard across live repositories:
1. Update the workflow files across the live repositories (`crosstrainingapp`, `retro-fighter`, `darwin-trader`, `gh-development-dashboard`, `kerberito`, `quant-trading-examples`).
2. Bump `standardVersion` and each updated repository's `syncedVersion` in `architecture-registry.json`.
3. Copy `architecture-registry.json` to `app/src/main/assets/architecture-registry.json` (enforced by unit tests and CI).

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
