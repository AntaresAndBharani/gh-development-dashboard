---
description: "Task list for Cross-Repository Architecture & Claude Code Review Compliance Tracking"
---

# Tasks: Cross-Repository Architecture & Claude Code Review Compliance Tracking

**Input**: Design documents from `/specs/001-architecture-compliance-tracking/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Tests**: Unit and Robolectric Compose UI tests are included in accordance with Constitution Principle III (Test-First & CI Parity).

**Organization**: Tasks are grouped by user story (`US1`, `US2`, `US3`) to enable independent implementation and verification.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (`[US1]`, `[US2]`, `[US3]`)
- Exact file paths are included in every task description.

---

## Phase 1: Setup (Canonical Registry & Project Assets)

**Purpose**: Create the canonical versioned Architecture Registry at the repository root and mirror it into the Android application assets directory.

- [X] T001 Create canonical versioned Architecture Registry defining `claude-code-review` (`standardVersion: "1.0.0"`, `lastUpdated: "2026-10-24"`, required workflow paths, and all 5 live `AntaresAndBharani` repositories) in `architecture-registry.json`
- [X] T002 [P] Mirror the canonical Architecture Registry into Android assets at `app/src/main/assets/architecture-registry.json`

---

## Phase 2: Foundational (Domain Models, GitHub API Contents Endpoint & Repository Compliance Logic)

**Purpose**: Core domain models, registry parser, GitHub REST API contents endpoint, and repository compliance evaluation required by all user stories.

**⚠️ CRITICAL**: Must be complete before user story UI and workflow tasks.

- [X] T003 Create `ComplianceState` (`UP_TO_DATE`, `OUTDATED`, `NOT_IMPLEMENTED`), `ArchitecturePracticeSpec`, `RepoArchitectureEntry`, `RepoComplianceStatus`, `ArchitectureComplianceSummary`, and `ArchitectureRegistry` (JSON parser + canonical 5-repo baseline + `evaluateRepoCompliance` and `buildArchitectureComplianceSummary` functions) in `app/src/main/java/com/example/domain/ArchitectureModels.kt`
- [X] T004 [P] Add `RepoContentDto` (`name`, `path`, `sha`, `type`) in `app/src/main/java/com/example/data/remote/Models.kt` and `getRepoFileContent(owner, repo, path)` (`@GET("repos/{owner}/{repo}/contents/{path}")`) with default fallback in `app/src/main/java/com/example/data/remote/GitHubService.kt`
- [X] T005 [P] Add optional `val complianceStatus: RepoComplianceStatus? = null` property to `ProjectHealth` in `app/src/main/java/com/example/domain/Models.kt`
- [X] T006 Implement `checkRepoArchitectureCompliance(owner, repo)` (inspecting `.github/workflows/claude.yml` and `.github/workflows/pr-review.yml` with 404 vs offline/rate-limit fallback to `ArchitectureRegistry`) and `seedLiveProjectsIfEmpty()` in `app/src/main/java/com/example/repository/ProjectRepository.kt`

**Checkpoint**: Foundational domain, remote API, and repository compliance evaluation ready.

---

## Phase 3: User Story 1 - Dashboard Architecture & Way of Working Compliance Overview Card (Priority: P1) 🎯 MVP

**Goal**: Display an expandable **"Architecture & Way of Working Compliance"** card on the main dashboard showing the target standard version (`v1.0.0`), aggregate compliance summary (`5/5 Up to Date`), and per-repository breakdown (`Implemented`, `Up to Date`, `Outdated`, `Not Implemented`, tech stack, synced version, and rollout date), while pre-seeding the 5 live `AntaresAndBharani` repositories on startup.

**Independent Test**: Verify `DashboardViewModel` exposes `architectureComplianceUiState: StateFlow<ArchitectureComplianceSummary>` with all 5 live repos, seeds empty storage when `autoSeedLiveProjects = true`, and `ArchitectureComplianceSection` renders aggregate counts and expands to show per-repository compliance rows.

### Tests for User Story 1

- [X] T007 [P] [US1] Create unit tests for `ArchitectureRegistry`, `evaluateRepoCompliance` (`UP_TO_DATE`, `OUTDATED`, `NOT_IMPLEMENTED`, offline fallback), and `buildArchitectureComplianceSummary` in `app/src/test/java/com/example/domain/ArchitectureComplianceTest.kt`
- [X] T008 [P] [US1] Add repository unit tests for `checkRepoArchitectureCompliance` (200 OK, 404 Not Found, and network exception fallback) and `seedLiveProjectsIfEmpty` in `app/src/test/java/com/example/repository/ProjectRepositoryTest.kt`
- [X] T009 [P] [US1] Add ViewModel unit tests for `architectureComplianceUiState` and live repository pre-seeding in `app/src/test/java/com/example/ui/DashboardViewModelTest.kt`

### Implementation for User Story 1

- [X] T010 [US1] Update `DashboardViewModel` and `DashboardViewModelFactory` to expose `architectureComplianceUiState: StateFlow<ArchitectureComplianceSummary>`, evaluate compliance for all tracked + canonical live repositories during project refresh, and support `autoSeedLiveProjects` in `app/src/main/java/com/example/ui/DashboardViewModel.kt`
- [X] T011 [US1] Create `ArchitectureComplianceSection` composable (expandable card with `testTag("architecture_compliance_section")`, standard version chip, compliance progress bar, status counters, and expandable per-repository compliance items) in `app/src/main/java/com/example/ui/ArchitectureComplianceSection.kt`
- [X] T012 [US1] Mount `ArchitectureComplianceSection` on `DashboardScreen` in `app/src/main/java/com/example/ui/DashboardScreen.kt`

**Checkpoint**: User Story 1 is functional and covered by unit tests.

---

## Phase 4: User Story 2 - Per-Repository Architecture Compliance Badge on Project Cards (Priority: P1)

**Goal**: Render a visible **Claude Code Review** compliance badge (`Claude Review: Up to Date · v1.0.0`, `Claude Review: Outdated`, or `Claude Review: Not Implemented`) on each `ProjectCard` in **Active Repositories**, plus detailed architecture metadata when the card is expanded.

**Independent Test**: Render `ProjectCard` and `ArchitectureComplianceSection` under Robolectric Compose UI tests and verify badge text, test tags, and expanded compliance details for `UP_TO_DATE`, `OUTDATED`, and `NOT_IMPLEMENTED` states.

### Tests for User Story 2

- [X] T013 [P] [US2] Create Robolectric Compose UI tests for `ArchitectureComplianceSection` and `ProjectCard` compliance badges across `UP_TO_DATE`, `OUTDATED`, and `NOT_IMPLEMENTED` states in `app/src/test/java/com/example/ui/ArchitectureComplianceUiTest.kt`

### Implementation for User Story 2

- [X] T014 [US2] Add `ProjectComplianceBadge` and expanded architecture standard details inside `ProjectCard` in `app/src/main/java/com/example/ui/DashboardScreen.kt`

**Checkpoint**: User Stories 1 and 2 are complete and verified by unit and Robolectric UI tests.

---

## Phase 5: User Story 3 - Synchronized Claude Code Review Rollout (`gh-development-dashboard` & `kerberito`) (Priority: P1)

**Goal**: Implement `.github/workflows/ci.yml`, `.github/workflows/claude.yml`, `.github/workflows/pr-review.yml`, and `.github/workflows/prompts/pr-review.md` in `gh-development-dashboard` (removing legacy `.github/workflows/merge.yml`), and roll out `.github/workflows/claude.yml`, `.github/workflows/pr-review.yml`, `.github/workflows/prompts/pr-review.md`, and `tests/test_claude_workflows.py` to `kerberito`.

**Independent Test**: Verify static workflow contracts in `ArchitectureComplianceTest.kt` for `gh-development-dashboard` and `tests/test_claude_workflows.py` (`pytest -v`) for `kerberito`.

### Implementation & Tests for User Story 3

- [X] T015 [P] [US3] Create `.github/workflows/ci.yml` (`name: CI`, running `./gradlew testDebugUnitTest --no-daemon` on `push` and `pull_request` to `main`) in `.github/workflows/ci.yml`
- [X] T016 [P] [US3] Create interactive `@claude` GitHub App workflow (`anthropics/claude-code-action@v1` with `--model claude-opus-5-5 --effort high`) in `.github/workflows/claude.yml`
- [X] T017 [P] [US3] Create automated PR review, in-place auto-fix, self-healing label governance, exact-SHA `CI` polling, squash-merge, and parent-story closure workflow in `.github/workflows/pr-review.yml` and remove legacy `.github/workflows/merge.yml`
- [X] T018 [P] [US3] Create automated PR review prompt tailored to `gh-development-dashboard` (MVVM + UDF, Room DB, Architecture Registry parity, Compose UI `testTag` coverage, and `gradle.properties` isolation) in `.github/workflows/prompts/pr-review.md`
- [X] T019 [US3] Add static contract assertions in `app/src/test/java/com/example/domain/ArchitectureComplianceTest.kt` verifying `architecture-registry.json` == `app/src/main/assets/architecture-registry.json` and validating `.github/workflows/ci.yml`, `.github/workflows/claude.yml`, `.github/workflows/pr-review.yml`, and `.github/workflows/prompts/pr-review.md`
- [X] T020 [US3] Roll out `.github/workflows/claude.yml`, `.github/workflows/pr-review.yml`, `.github/workflows/prompts/pr-review.md`, and `tests/test_claude_workflows.py` in `c:/Users/rogal/workspaces/ws-setups/kerberito`, run `pytest -v`, and commit/open PR for `kerberito`

**Checkpoint**: All 5 live repositories (`crosstrainingapp`, `retro-fighter`, `darwin-trader`, `gh-development-dashboard`, `kerberito`) have the v1.0.0 Claude Code Review workflows and pass static contract verification.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final documentation, changelog update, and full test suite verification.

- [X] T021 Update `CHANGELOG.md` under `## [Unreleased]` and `README.md` in `gh-development-dashboard` documenting the Architecture & Way of Working Compliance tracking feature, `architecture-registry.json`, and Claude GitHub App workflows
- [X] T022 Run full unit and Robolectric UI test suite (`.\gradlew.bat testDebugUnitTest --no-daemon`) in `gh-development-dashboard` and `pytest -v` in `kerberito` to confirm 100% green execution

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies — creates `architecture-registry.json` and asset mirror.
- **Phase 2 (Foundational)**: Depends on Phase 1 — builds domain models, `GitHubService` endpoint, and `ProjectRepository` compliance evaluation.
- **Phase 3 (US1 - Overview Card & Seeding)**: Depends on Phase 2.
- **Phase 4 (US2 - Per-Repo Card Badge)**: Depends on Phase 2 and integrates with `DashboardScreen.kt` from Phase 3.
- **Phase 5 (US3 - Claude Workflows in `gh-development-dashboard` & `kerberito`)**: Can run in parallel with Phases 3–4; `T019` validates both registry parity and workflow files.
- **Phase 6 (Polish)**: Depends on Phases 1–5 completion.

### Parallel Opportunities

- `T001` and `T002` create the root and asset registry files.
- `T003`, `T004`, and `T005` touch distinct files (`ArchitectureModels.kt`, `GitHubService.kt`/`Models.kt`, `domain/Models.kt`).
- `T015`, `T016`, `T017`, `T018`, and `T020` create workflow files across `gh-development-dashboard` and `kerberito` in parallel.

---

## Implementation Strategy

### Incremental Delivery

1. Complete Phase 1 & Phase 2 (Registry + Domain + Repository foundation).
2. Complete Phase 3 & Phase 4 (ViewModel + Dashboard Overview Card + ProjectCard Badges + Unit/UI Tests).
3. Complete Phase 5 (`gh-development-dashboard` and `kerberito` Claude Code Review workflows + contract tests).
4. Complete Phase 6 (Documentation, `CHANGELOG.md`, full test verification, and PR creation).
