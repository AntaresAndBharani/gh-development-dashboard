# Implementation Plan: Quant Trading Examples Workflow Rollout & Architecture Tracking

**Branch**: `feat/002-quant-trading-workflows` | **Date**: 2026-10-31 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/002-quant-trading-workflows/spec.md`

## Summary

Deploy the standardized v1.0.0 Claude Code Review & CI workflow architecture (`.github/workflows/ci.yml`, `.github/workflows/claude.yml`, `.github/workflows/pr-review.yml`, `.github/workflows/prompts/pr-review.md`, and `tests/test_claude_workflows.py`) to `repo-examples/quant-trading-examples` (`C:\Users\rogal\workspaces\ws-trading\quant-trading-examples`), tailored to its `quantlab` Python 3.11+ quantitative research stack (`ruff`, `pytest`, `quantlab dashboard --check`, PIT causality, SymPy+Numba indicators, information-theoretic/statistical/DSP proofs, and Pine Script v6 transpiler). Simultaneously register `repo-examples/quant-trading-examples` as the 6th tracked repository in `gh-development-dashboard` (`architecture-registry.json`, `app/src/main/assets/architecture-registry.json`, `ArchitectureModels.kt`, `ProjectRepository.kt`, and unit/Robolectric UI tests).

## Technical Context

**Language/Version**: Kotlin 2.2.10 (JVM 17) for `gh-development-dashboard`; Python 3.11+ & GitHub Actions YAML for `repo-examples/quant-trading-examples`.

**Primary Dependencies**: Jetpack Compose (Material 3), Room 2.7.0, Retrofit 2.12.0, Moshi 1.15.2, Coroutines 1.9.0 in `gh-development-dashboard`; `pytest`, `ruff`, `typer`, `sympy`, `numba`, `scipy`, `streamlit` in `repo-examples/quant-trading-examples`.

**Storage**: Room SQLite (`projects` and `status_transitions`) + Canonical Architecture Registry (`architecture-registry.json` & `app/src/main/assets/architecture-registry.json`).

**Testing**: JUnit 4, Kotlin Coroutines Test, Robolectric 4.16.1, Compose UI Test (`.\gradlew.bat testDebugUnitTest --no-daemon`) in `gh-development-dashboard`; `uv run pytest -v` and `uv run ruff check src tests` in `repo-examples/quant-trading-examples`.

**Target Platform**: Android (compileSdk 36, minSdk 24, targetSdk 36) & GitHub Actions (`ubuntu-latest`).

**Project Type**: Android Application + Multi-Repo GitHub Actions DevOps Workflows.

**Performance Goals**: Non-blocking coroutine compliance evaluation across all 6 tracked repositories; deterministic CI verification in `< 2 minutes`.

**Constraints**: Offline-first registry fallback; byte-for-byte parity between root `architecture-registry.json` and `app/src/main/assets/architecture-registry.json`.

**Scale/Scope**: 6 tracked repositories (`5` in `AntaresAndBharani` + `repo-examples/quant-trading-examples`).

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **I. MVVM & Unidirectional Data Flow (UDF)**: **PASS** — `DashboardViewModel` exposes immutable `StateFlow` for the 6-repository compliance summary.
- **II. Offline-First Persistence & Versioned Architecture Registry**: **PASS** — Both `architecture-registry.json` and `app/src/main/assets/architecture-registry.json` are updated in lockstep with `ArchitectureRegistry.LIVE_REPOSITORIES`.
- **III. Test-First & Continuous Integration Parity**: **PASS** — Covered by `tests/test_claude_workflows.py` in `quant-trading-examples` and `ArchitectureComplianceTest`, `ProjectRepositoryTest`, `DashboardViewModelTest`, and `ArchitectureComplianceUiTest` in `gh-development-dashboard`.
- **IV. Cross-Repository Claude GitHub App & SDLC Governance**: **PASS** — `repo-examples/quant-trading-examples` receives the full v1.0.0 workflow suite (`ci.yml`, `claude.yml`, `pr-review.yml`, `prompts/pr-review.md`).
- **V. Environment Isolation & Repository Hygiene**: **PASS** — No machine-specific paths committed; `CHANGELOG.md` updated in both repositories.

## Project Structure

### Documentation (this feature)

```text
specs/002-quant-trading-workflows/
├── plan.md              # This implementation plan
├── research.md          # Phase 0 technical research & decisions
├── data-model.md        # Phase 1 registry & workflow schema models
├── quickstart.md        # Phase 1 verification guide
├── contracts/
│   └── quantlab-workflows-contract.md
├── checklists/
│   ├── requirements.md
│   └── workflow-compliance.md
└── tasks.md             # Phase 2 actionable task breakdown
```

### Source Code (`gh-development-dashboard`)

```text
gh-development-dashboard/
├── architecture-registry.json
├── CHANGELOG.md
├── README.md
├── app/
│   └── src/
│       ├── main/
│       │   ├── assets/
│       │   │   └── architecture-registry.json
│       │   └── java/com/example/
│       │       ├── domain/
│       │       │   └── ArchitectureModels.kt
│       │       └── repository/
│       │           └── ProjectRepository.kt
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

### Cross-Repository Target (`repo-examples/quant-trading-examples`)

```text
C:/Users/rogal/workspaces/ws-trading/quant-trading-examples/
├── .github/
│   └── workflows/
│       ├── ci.yml
│       ├── claude.yml
│       ├── pr-review.yml
│       └── prompts/
│           └── pr-review.md
├── tests/
│   └── test_claude_workflows.py
└── CHANGELOG.md
```

**Structure Decision**: Deploys the adapted v1.0.0 workflow suite and static contract tests directly into `C:/Users/rogal/workspaces/ws-trading/quant-trading-examples` and updates the canonical 6-repository Architecture Registry, seeding logic, and test suite in `gh-development-dashboard`.

## Complexity Tracking

No Constitution Check violations.
