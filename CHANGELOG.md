# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed
- Made `StageTransition.issueId` a required constructor parameter without default value in `AnalyticsModels.kt` to prevent silent cross-issue transition collisions.

### Added
- Story Evolution Chart and Stage Dwell-Time Cards UI with Material 3 Scope Bar and time-window filtering (`AnalyticsScreen.kt`).
- `AnalyticsUiState` and `DashboardViewModel` wiring for `3D`, `14D`, `30D` time windows and Global vs per-project filtering.
- Dwell-time calculation domain logic (`calculateStageDwellTimes`, `calculateDwellTimeSummary`) for the 9-phase SDLC pipeline in `AnalyticsModels.kt`.
- Room DB caching for stage transition timeline events via `StatusTransitionEntity`, `StatusTransitionDao`, and `ProjectRepository.fetchAndCacheIssueTransitions`.
- GitHub issue events endpoint and `IssueEventDto` data model in `GitHubService` and `Models.kt` for stage transition tracking.
- Merge & Backlog autonomous workflow in `.github/workflows/merge.yml`.
- Graph Engineering autonomous SDLC node infrastructure (Architect, Three Amigos, Dev & Test, PR Review, Backlog Triage).
- Local CLI Pipeline execution scripts in `scripts/local-pipeline/` with Windows Task Scheduler integration (`GDD-*`).
- SMART GitHub issue templates for `user-story` and `subtask` under `.github/ISSUE_TEMPLATE/`.
- Antigravity agent personas and task prompts under `.antigravity/` and `.claude/tasks/`.
- Unit test summarizer script `scripts/summarize-unit-tests.ps1`.
- Project instructions and guidelines in `GEMINI.md`.

## [1.0.0] - 2026-08-27

### Added
- GitHub Actions manual release workflow `.github/workflows/release.yml` with APK building and release publishing.
- Gradle wrapper binaries (`gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`).
- Initial Android Jetpack Compose GitHub Development Dashboard application.
