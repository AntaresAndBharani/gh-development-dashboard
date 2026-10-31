---
description: "Task list for Quant Trading Examples Workflow Rollout & Architecture Tracking"
---

# Tasks: Quant Trading Examples Workflow Rollout & Architecture Tracking

**Input**: Design documents from `/specs/002-quant-trading-workflows/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Tests**: Unit, static contract, and Robolectric Compose UI tests are included in accordance with Constitution Principle III (Test-First & CI Parity).

**Organization**: Tasks are grouped by phase and user story (`US1`, `US2`) to enable independent implementation and verification.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (`[US1]`, `[US2]`)
- Exact file paths are included in every task description.

---

## Phase 1: Setup (Canonical Registry & Asset Mirror)

**Purpose**: Register `repo-examples/quant-trading-examples` in the canonical Architecture Registry at the repository root and mirror it in the Android assets directory.

- [X] T001 Update `architecture-registry.json` (`lastUpdated: "2026-10-31"`) to add `repo-examples/quant-trading-examples` as the 6th tracked repository at `standardVersion: "1.0.0"` in `architecture-registry.json`
- [X] T002 [P] Mirror the updated 6-repository Architecture Registry into Android assets at `app/src/main/assets/architecture-registry.json`

---

## Phase 2: Foundational (Domain Baseline & Idempotent Seeding)

**Purpose**: Update `ArchitectureRegistry.LIVE_REPOSITORIES` and `ProjectRepository.seedLiveProjectsIfEmpty()` so all 6 canonical repositories are tracked and seeded idempotently.

- [X] T003 Add `repo-examples/quant-trading-examples` to `ArchitectureRegistry.LIVE_REPOSITORIES` and update `DEFAULT_PRACTICE.lastUpdated = "2026-10-31"` in `app/src/main/java/com/example/domain/ArchitectureModels.kt`
- [X] T004 [P] Update `seedLiveProjectsIfEmpty()` in `app/src/main/java/com/example/repository/ProjectRepository.kt` to idempotently insert any missing canonical repositories from `architectureRegistry.repositories`

**Checkpoint**: Foundational 6-repository registry and repository seeding logic ready.

---

## Phase 3: User Story 1 - Adapted Claude Code Review & CI Workflows in `repo-examples/quant-trading-examples` (Priority: P1) 🎯 MVP

**Goal**: Deploy `.github/workflows/ci.yml`, `.github/workflows/claude.yml`, `.github/workflows/pr-review.yml`, `.github/workflows/prompts/pr-review.md`, and `tests/test_claude_workflows.py` tailored to `quantlab` in `repo-examples/quant-trading-examples`.

**Independent Test**: Run `uv run pytest -v`, `uv run ruff check src tests`, and `uv run quantlab dashboard --check` inside `repo-examples/quant-trading-examples`.

### Implementation & Tests for User Story 1

- [X] T005 [P] [US1] Create `.github/workflows/ci.yml` (`name: CI`, running `ruff check src tests`, `pytest -v`, and `quantlab dashboard --check` on `push` and `pull_request` to `main`) in `repo-examples/quant-trading-examples:.github/workflows/ci.yml`
- [X] T006 [P] [US1] Create interactive `@claude` GitHub App workflow (`name: Claude Code`, `anthropics/claude-code-action@v1` with `--model claude-opus-5-5 --effort high`) in `repo-examples/quant-trading-examples:.github/workflows/claude.yml`
- [X] T007 [P] [US1] Create automated PR review, in-place auto-fix (`[skip pr-review]`), zero-token `merge_only` fast path, label governance, exact-SHA `CI` polling, squash-merge, and parent-story closure workflow in `repo-examples/quant-trading-examples:.github/workflows/pr-review.yml`
- [X] T008 [P] [US1] Create tailored PR review prompt for `QuantLab` (`repo-examples/quant-trading-examples`) covering PIT causality, SymPy+Numba dual indicators, entropy/stats/DSP proof gates, Pine Script v6 transpiler, 4-quadrant Streamlit dashboard, and `pytest`/`ruff` hygiene in `repo-examples/quant-trading-examples:.github/workflows/prompts/pr-review.md`
- [X] T009 [US1] Create static workflow contract tests in `repo-examples/quant-trading-examples:tests/test_claude_workflows.py` and update `repo-examples/quant-trading-examples:CHANGELOG.md`
- [X] T010 [US1] Run `uv run pytest -v`, `uv run ruff check src tests`, and `uv run quantlab dashboard --check` in `repo-examples/quant-trading-examples`, commit on branch `feat/001-quantlab-research-suite`, and push to `repo-examples/quant-trading-examples`

**Checkpoint**: User Story 1 is complete and verified in `repo-examples/quant-trading-examples`.

---

## Phase 4: User Story 2 - Register and Track `repo-examples/quant-trading-examples` in `gh-development-dashboard` (Priority: P1)

**Goal**: Update unit and Robolectric Compose UI tests in `gh-development-dashboard` to verify all 6 tracked repositories (`6/6 Up to Date`), including `repo-examples/quant-trading-examples`.

**Independent Test**: Run `.\gradlew.bat testDebugUnitTest --no-daemon` in `gh-development-dashboard` and verify 100% passing tests.

### Tests for User Story 2

- [X] T011 [P] [US2] Update `app/src/test/java/com/example/domain/ArchitectureComplianceTest.kt` to assert 6 canonical repositories (including `repo-examples/quant-trading-examples`), byte-for-byte asset parity, and aggregate compliance summary counts
- [X] T012 [P] [US2] Update `app/src/test/java/com/example/repository/ProjectRepositoryTest.kt` to verify `seedLiveProjectsIfEmpty` inserts all 6 canonical repositories and idempotently backfills missing canonical entries
- [X] T013 [P] [US2] Update `app/src/test/java/com/example/ui/DashboardViewModelTest.kt` to verify 6 canonical repositories initialize as `UP_TO_DATE` and auto-seed on startup
- [X] T014 [P] [US2] Update `app/src/test/java/com/example/ui/ArchitectureComplianceUiTest.kt` to assert `"6/6 Up to Date"` and verify `architecture_repo_row_quant-trading-examples` is displayed when expanded

**Checkpoint**: User Stories 1 and 2 are complete and verified by unit and Robolectric UI tests.

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Final documentation, changelog updates, and full test suite verification.

- [X] T015 Update `CHANGELOG.md` under `## [Unreleased]` and `README.md` in `gh-development-dashboard` documenting the onboarding of `repo-examples/quant-trading-examples` into the Architecture Registry
- [X] T016 Run full unit and Robolectric UI test suite (`.\gradlew.bat testDebugUnitTest --no-daemon`) in `gh-development-dashboard` to confirm 100% green execution

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies — updates `architecture-registry.json` and `app/src/main/assets/architecture-registry.json`.
- **Phase 2 (Foundational)**: Depends on Phase 1 — updates `ArchitectureModels.kt` and `ProjectRepository.kt`.
- **Phase 3 (US1 - Workflows in `repo-examples/quant-trading-examples`)**: Independent of Android changes; can execute in parallel with Phases 1–2.
- **Phase 4 (US2 - Dashboard Registry & UI Tests)**: Depends on Phases 1 and 2.
- **Phase 5 (Polish)**: Depends on Phases 1–4 completion.

### Parallel Opportunities

- `T001` and `T002` update root and asset registry files in parallel.
- `T005`, `T006`, `T007`, and `T008` create the 4 workflow files in `repo-examples/quant-trading-examples` in parallel.
- `T011`, `T012`, `T013`, and `T014` update distinct test files in `gh-development-dashboard` in parallel.

---

## Implementation Strategy

### Incremental Delivery

1. Complete Phase 1 & Phase 2 (6-repository Registry + Domain + Repository seeding).
2. Complete Phase 3 (Deploy workflows & tests in `repo-examples/quant-trading-examples`, verify with `pytest` + `ruff`, and push).
3. Complete Phase 4 & Phase 5 (Update `gh-development-dashboard` tests, `CHANGELOG.md`, `README.md`, verify `testDebugUnitTest`, and open PR).
