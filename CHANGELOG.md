# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed
- Expanded `architecture-registry.json`, `app/src/main/assets/architecture-registry.json`, and `ArchitectureRegistry.LIVE_REPOSITORIES` to track 6 live repositories (`AntaresAndBharani/crosstrainingapp`, `AntaresAndBharani/retro-fighter`, `AntaresAndBharani/darwin-trader`, `AntaresAndBharani/gh-development-dashboard`, `AntaresAndBharani/kerberito`, and `repo-examples/quant-trading-examples`) at Claude Code Review standard `v1.0.0`.
- Updated `ProjectRepository.seedLiveProjectsIfEmpty()` to idempotently insert any missing canonical repositories into Room storage when upgrading existing installations.
- Amended `.specify/memory/constitution.md` to `v1.1.0`, extending Principle IV (Cross-Repository Claude GitHub App & SDLC Governance) to all live tracked repositories across `AntaresAndBharani` and `repo-examples`.
- Replaced legacy label-triggered `.github/workflows/merge.yml` with the standardized `AntaresAndBharani` Claude Code Review & Auto-Fix workflow (`.github/workflows/pr-review.yml`) and interactive `@claude` workflow (`.github/workflows/claude.yml`).
- Made `StageTransition.issueId` a required constructor parameter without default value in `AnalyticsModels.kt` to prevent silent cross-issue transition collisions.

### Added
- **Quant Trading Examples (`repo-examples/quant-trading-examples`) Workflow Onboarding**:
  - Added `.github/workflows/ci.yml`, `.github/workflows/claude.yml`, `.github/workflows/pr-review.yml`, `.github/workflows/prompts/pr-review.md`, and `tests/test_claude_workflows.py` in `repo-examples/quant-trading-examples` tailored to `quantlab`, and registered it in `gh-development-dashboard`.
- **Architecture & Way of Working Compliance Tracking (`v1.0.0`)**:
  - Canonical versioned `architecture-registry.json` (mirrored to `app/src/main/assets/architecture-registry.json`) tracking the 6 live repositories (`crosstrainingapp`, `retro-fighter`, `darwin-trader`, `gh-development-dashboard`, `kerberito`, `quant-trading-examples`) and their Claude Code Review standard version (`1.0.0`).
  - Pure domain models and evaluator (`ArchitectureModels.kt`, `ArchitectureRegistry`) combining live GitHub Contents API workflow checks (`.github/workflows/claude.yml`, `.github/workflows/pr-review.yml`) with registry version comparison (`UP_TO_DATE`, `OUTDATED`, `NOT_IMPLEMENTED`, plus offline fallback).
  - Expandable `ArchitectureComplianceSection` summary card and per-project `ProjectComplianceBadge` / `ProjectComplianceDetailsCard` on `DashboardScreen.kt`, with automatic seeding of the live repositories on launch.
  - Standardized GitHub Actions `CI` workflow (`.github/workflows/ci.yml`), `Claude Code` workflow (`.github/workflows/claude.yml`), `PR Review` workflow (`.github/workflows/pr-review.yml`), and review prompt (`.github/workflows/prompts/pr-review.md`).
- Story Evolution Chart and Stage Dwell-Time Cards UI with Material 3 Scope Bar and time-window filtering (`AnalyticsScreen.kt`).
- `AnalyticsUiState` and `DashboardViewModel` wiring for `3D`, `14D`, `30D` time windows and Global vs per-project filtering.
- Dwell-time calculation domain logic (`calculateStageDwellTimes`, `calculateDwellTimeSummary`) for the 9-phase SDLC pipeline in `AnalyticsModels.kt`.
- Room DB caching for stage transition timeline events via `StatusTransitionEntity`, `StatusTransitionDao`, and `ProjectRepository.fetchAndCacheIssueTransitions`.
- GitHub issue events endpoint and `IssueEventDto` data model in `GitHubService` and `Models.kt` for stage transition tracking.
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
