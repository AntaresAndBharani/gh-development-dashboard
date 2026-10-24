# GitHub Development Dashboard (GitScope) Constitution

## Core Principles

### I. MVVM & Unidirectional Data Flow (UDF)
Every UI feature MUST follow strict Model-View-ViewModel (MVVM) separation with Unidirectional Data Flow (`Composable -> ViewModel -> Repository -> Room / Retrofit GitHub API`). ViewModels MUST expose immutable `StateFlow` streams collected via `collectAsStateWithLifecycle()` in Jetpack Compose screens, and UI components MUST NOT invoke network or database operations directly (except read-only detail sheets delegating through defined data clients).

### II. Offline-First Persistence & Versioned Architecture Registry
Repository metadata, status transitions, and architecture compliance baselines MUST support offline-first resilience. Cross-repository Architecture & Way of Working compliance (such as Claude Code Review adoption and version currency across heterogeneous tech stacks) MUST combine live GitHub API workflow detection (`.github/workflows/claude.yml` and `.github/workflows/pr-review.yml`) with a canonical, versioned Architecture Registry stored in this repository (`architecture-registry.json` and mirrored in `app/src/main/assets/architecture-registry.json`).

### III. Test-First & Continuous Integration Parity (NON-NEGOTIABLE)
All domain calculations, repository logic, Room DAO operations, and Jetpack Compose UI components MUST be covered by deterministic unit and Robolectric tests (`.\gradlew.bat testDebugUnitTest --no-daemon`). No Pull Request may be merged unless 100% of unit tests and static workflow contract checks pass in CI.

### IV. Cross-Repository Claude GitHub App & SDLC Governance
All live `AntaresAndBharani` repositories MUST implement the standardized Claude GitHub App workflow architecture: `.github/workflows/claude.yml` for interactive `@claude` mentions and `.github/workflows/pr-review.yml` + `.github/workflows/prompts/pr-review.md` for automated Claude Opus 5.5 High PR review, in-place auto-fix (`[skip pr-review]`), `cancel-in-progress: false` concurrency, zero-token `merge_only` fast-path recovery, self-healing label governance, exact-SHA CI polling, squash-merge, and parent-story closure.

### V. Environment Isolation & Repository Hygiene
Machine-specific paths (such as `org.gradle.java.home` or local SDK paths) MUST NEVER be committed to `gradle.properties` or tracked files. Secrets MUST reside in `.env` (git-ignored) or GitHub Actions secrets (`CLAUDE_CODE_OAUTH_TOKEN`, `ORCHESTRATION_PAT`). Every feature and fix MUST be documented under `## [Unreleased]` in `CHANGELOG.md`.

## Technical Stack & Architectural Constraints

- **Platform & SDK**: Android (`compileSdk = 36`, `minSdk = 24`, `targetSdk = 36`), Kotlin 2.2.10, JVM target 17.
- **UI Framework**: Jetpack Compose (Material 3) with responsive card layouts and explicit `testTag` semantics for automated Robolectric UI testing.
- **Networking & Persistence**: Retrofit 2 + OkHttp + Moshi for GitHub REST API integration; Room 2.7.0 for local persistence.
- **Live Managed Repositories**: `AntaresAndBharani/crosstrainingapp`, `AntaresAndBharani/retro-fighter`, `AntaresAndBharani/darwin-trader`, `AntaresAndBharani/gh-development-dashboard`, and `AntaresAndBharani/kerberito`.

## Development Workflow & Quality Gates

1. **Branching & Specification**: Features are developed on `feat/<NNN-short-name>` branches backed by Spec-Kit artifacts under `specs/<NNN-short-name>/`.
2. **Automated Verification**: Local verification via `.\gradlew.bat testDebugUnitTest --no-daemon` and CI verification via `.github/workflows/ci.yml` (`name: CI`).
3. **Automated PR Review & Merge**: Non-draft Pull Requests are reviewed by `.github/workflows/pr-review.yml`, which gates squash-merging on `review:approved` and `completed:success` of the `CI` workflow for the exact `HEAD` commit SHA.

## Governance

This Constitution supersedes ad-hoc practices within `gh-development-dashboard`. Amendments require updating `.specify/memory/constitution.md`, incrementing `CONSTITUTION_VERSION` according to semantic versioning, and verifying alignment across `GEMINI.md` and `.github/workflows/prompts/pr-review.md`.

**Version**: 1.0.0 | **Ratified**: 2026-10-24 | **Last Amended**: 2026-10-24
