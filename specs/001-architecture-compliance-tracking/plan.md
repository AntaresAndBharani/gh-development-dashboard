# Implementation Plan: Cross-Repository Architecture & Claude Code Review Compliance Tracking

**Branch**: `feat/001-architecture-compliance-tracking` | **Date**: 2026-10-24 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/001-architecture-compliance-tracking/spec.md`

## Summary

Track organization-wide **Architecture & Way of Working** adoption across all live `AntaresAndBharani` repositories in `gh-development-dashboard`, starting with **Claude Code Review** (`.github/workflows/claude.yml`, `.github/workflows/pr-review.yml`, `.github/workflows/prompts/pr-review.md`). Because the managed repositories use heterogeneous technology stacks (Android/Kotlin, Godot/GDScript, Python/React, Python TUI/CLI), `gh-development-dashboard` stores a canonical versioned **Architecture Registry** (`architecture-registry.json` at the repo root and `app/src/main/assets/architecture-registry.json`) and combines it with live GitHub REST API workflow file detection (`GET /repos/{owner}/{repo}/contents/{path}`) to determine whether each repository has implemented the practice and whether its implementation is up to date. Additionally, roll out the standardized Claude Code Review workflows to the remaining live repositories (`gh-development-dashboard` and `kerberito`) so all 5 live repositories (`crosstrainingapp`, `retro-fighter`, `darwin-trader`, `gh-development-dashboard`, and `kerberito`) are aligned at version `1.0.0`.

## Technical Context

**Language/Version**: Kotlin 2.2.10 (JVM 17) for `gh-development-dashboard`; Python 3.11+ & GitHub Actions YAML for `kerberito` and CI workflows.

**Primary Dependencies**: Jetpack Compose (BOM 2024.09.00, Material 3), AndroidX Lifecycle (`collectAsStateWithLifecycle`), Room 2.7.0, Retrofit 2.12.0, OkHttp 4.10.0, Moshi 1.15.2, Coroutines 1.9.0.

**Storage**: Room SQLite (`projects` and `status_transitions` tables) + Canonical Versioned Architecture Registry (`architecture-registry.json` & `app/src/main/assets/architecture-registry.json`).

**Testing**: JUnit 4, Kotlin Coroutines Test, Robolectric 4.16.1, Compose UI Test (`createComposeRule()`); `pytest` for `kerberito`.

**Target Platform**: Android (compileSdk 36, minSdk 24, targetSdk 36) & GitHub Actions (`ubuntu-latest`).

**Project Type**: Mobile Android Application (`com.example` / `com.aistudio.githubdash.kjhgf`) + GitHub Actions DevOps Workflows.

**Performance Goals**: Non-blocking parallel coroutine evaluation of repository workflow compliance during project refresh; 60 fps Compose rendering.

**Constraints**: Offline-first resilience (graceful fallback to `ArchitectureRegistry` when GitHub API is unreachable or rate-limited); zero machine-specific paths in `gradle.properties`.

**Scale/Scope**: 5 pre-seeded live `AntaresAndBharani` repositories (`crosstrainingapp`, `retro-fighter`, `darwin-trader`, `gh-development-dashboard`, `kerberito`) plus any user-added repositories.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **I. MVVM & Unidirectional Data Flow (UDF)**: **PASS** — `ArchitectureComplianceSection` and `ProjectCard` badges read immutable `StateFlow<ArchitectureComplianceSummary>` and `StateFlow<List<ProjectHealth>>` exposed by `DashboardViewModel` and collected via `collectAsStateWithLifecycle()`.
- **II. Offline-First Persistence & Versioned Architecture Registry**: **PASS** — Canonical `architecture-registry.json` is stored at the repository root and mirrored in `app/src/main/assets/architecture-registry.json`, with fallback when GitHub API calls fail offline.
- **III. Test-First & Continuous Integration Parity**: **PASS** — Full unit and Robolectric Compose UI test coverage in `app/src/test/` verified via `.\gradlew.bat testDebugUnitTest --no-daemon` and `.github/workflows/ci.yml`.
- **IV. Cross-Repository Claude GitHub App & SDLC Governance**: **PASS** — Both `gh-development-dashboard` and `kerberito` receive `.github/workflows/claude.yml`, `.github/workflows/pr-review.yml`, and `.github/workflows/prompts/pr-review.md` (with `CI_WORKFLOW_NAME: "CI"`).
- **V. Environment Isolation & Repository Hygiene**: **PASS** — No machine-specific paths in tracked files; `CHANGELOG.md` updated under `## [Unreleased]`.

## Project Structure

### Documentation (this feature)

```text
specs/001-architecture-compliance-tracking/
├── plan.md              # This implementation plan
├── research.md          # Phase 0 technical research & decisions
├── data-model.md        # Phase 1 domain, DTO, and state models
├── quickstart.md        # Phase 1 verification guide
├── contracts/           # Phase 1 JSON schema and GitHub API contracts
│   ├── architecture-registry.schema.json
│   └── github-contents-api.md
├── checklists/          # Specification & domain quality checklists
│   ├── requirements.md
│   └── architecture-compliance.md
└── tasks.md             # Phase 2 actionable task breakdown
```

### Source Code (repository root)

```text
gh-development-dashboard/
├── architecture-registry.json
├── .github/
│   └── workflows/
│       ├── ci.yml
│       ├── claude.yml
│       ├── pr-review.yml
│       ├── release.yml
│       └── prompts/
│           └── pr-review.md
├── app/
│   └── src/
│       ├── main/
│       │   ├── assets/
│       │   │   └── architecture-registry.json
│       │   └── java/com/example/
│       │       ├── MainActivity.kt
│       │       ├── data/
│       │       │   └── remote/
│       │       │       ├── GitHubService.kt
│       │       │       └── Models.kt
│       │       ├── domain/
│       │       │   ├── ArchitectureModels.kt
│       │       │   └── Models.kt
│       │       ├── repository/
│       │       │   └── ProjectRepository.kt
│       │       └── ui/
│       │           ├── ArchitectureComplianceSection.kt
│       │           ├── DashboardScreen.kt
│       │           └── DashboardViewModel.kt
│       └── test/
│           └── java/com/example/
│               ├── domain/
│               │   └── ArchitectureComplianceTest.kt
│               ├── repository/
│               │   └── ProjectRepositoryTest.kt
│               └── ui/
│                   ├── ArchitectureComplianceUiTest.kt
│                   └── DashboardViewModelTest.kt
```

**Cross-Repository Target (`kerberito`)**:
```text
c:/Users/rogal/workspaces/ws-setups/kerberito/
├── .github/
│   └── workflows/
│       ├── ci.yml
│       ├── claude.yml
│       ├── pr-review.yml
│       └── prompts/
│           └── pr-review.md
└── tests/
    └── test_claude_workflows.py
```

**Structure Decision**: Extends the existing single-module Android MVVM structure (`com.example.data`, `com.example.domain`, `com.example.repository`, `com.example.ui`) in `gh-development-dashboard`, stores `architecture-registry.json` at both the repo root and `app/src/main/assets/`, and deploys the standardized `.github/workflows/{claude.yml,pr-review.yml,prompts/pr-review.md}` workflow suite to both `gh-development-dashboard` and `kerberito`.

## Complexity Tracking

No Constitution Check violations.
