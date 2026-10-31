# Feature Specification: Quant Trading Examples Workflow Rollout & Architecture Tracking

**Feature Branch**: `feat/002-quant-trading-workflows`

**Created**: 2026-10-31

**Status**: Approved

**Input**: User description: "I want to setup in this repo repo-examples/quant-trading-example the workflows setup we have for the rest of the repositories we track (adapted to the repo, obviously)."

## Clarifications

### Session 2026-10-31

- Q: On GitHub and in the local workspace (`quant-trading-examples` checkout), the repository is named `repo-examples/quant-trading-examples` (`quantlab`). What scope should we execute across `repo-examples/quant-trading-examples` and `gh-development-dashboard`? → A: Set up the adapted Claude Code Review + CI workflows (`ci.yml`, `claude.yml`, `pr-review.yml`, `prompts/pr-review.md`, and workflow contract tests) in `repo-examples/quant-trading-examples` AND register it in `gh-development-dashboard` (`architecture-registry.json`, `ArchitectureModels.kt`, and tests) as the 6th tracked repository.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Adapted Claude Code Review & CI Workflows in `repo-examples/quant-trading-examples` (Priority: P1)

As a maintainer of `repo-examples/quant-trading-examples` (`quantlab`), I want the standardized Claude Code Review (`claude.yml`, `pr-review.yml`, `prompts/pr-review.md`) and Continuous Integration (`ci.yml`) workflows deployed and tailored to `quantlab`'s quantitative research stack (Point-in-Time causal alignment, SymPy + Numba neuro-symbolic indicator registry, information-theoretic/statistical/DSP proof gates, Pine Script v6 transpiler, Streamlit diagnostic dashboard, `pytest`, and `ruff`), so that interactive `@claude` assistance and automated Pull Request review, auto-fix, CI verification, and squash-merging operate identically to the rest of our tracked repositories.

**Why this priority**: Directly delivers the core workflow automation requested for `repo-examples/quant-trading-examples` and enforces architectural, mathematical, and test-parity gates on every Pull Request.

**Independent Test**: Can be independently tested by running the automated workflow contract test suite (`tests/test_claude_workflows.py`) and full `pytest` + `ruff` verification inside `repo-examples/quant-trading-examples`.

**Acceptance Scenarios**:

1. **Given** a commit is pushed to `main` or a Pull Request targeting `main` is opened or synchronized in `repo-examples/quant-trading-examples`, **When** the `CI` workflow (`.github/workflows/ci.yml`) runs, **Then** it installs the project and dev dependencies, executes `ruff check src tests`, runs `pytest -v`, and verifies headless dashboard self-check (`quantlab dashboard --check`).
2. **Given** a collaborator comments `@claude` on an issue, pull request, or review in `repo-examples/quant-trading-examples`, **When** `.github/workflows/claude.yml` triggers, **Then** Claude Opus 5.5 High executes with write permissions and repository context.
3. **Given** a non-draft Pull Request targeting `main` is opened or updated in `repo-examples/quant-trading-examples`, **When** `.github/workflows/pr-review.yml` executes, **Then** Claude Opus 5.5 High reviews the diff against `.github/workflows/prompts/pr-review.md` (checking PIT zero-lookahead causality, dual SymPy/Numba parity, statistical/entropy/DSP proof gates, Pine Script v6 transpiler safety, and `pytest`/`ruff` hygiene), applies in-place auto-fixes with `[skip pr-review]`, manages review labels, waits for the exact-SHA `CI` run to succeed, squash-merges the PR, and closes the parent story when all subtasks complete.

---

### User Story 2 - Register and Track `repo-examples/quant-trading-examples` in `gh-development-dashboard` (Priority: P1)

As an engineering lead using `gh-development-dashboard`, I want `repo-examples/quant-trading-examples` registered in the canonical Architecture Registry (`architecture-registry.json` and `app/src/main/assets/architecture-registry.json`) and pre-seeded into the tracked repositories list so the dashboard displays all 6 tracked repositories (`5` under `AntaresAndBharani` + `repo-examples/quant-trading-examples`) as `Up to Date` at standard version `1.0.0`.

**Why this priority**: Keeps the single-pane-of-glass Architecture & Way of Working Compliance view and Active Repositories list synchronized with the newly onboarded `repo-examples/quant-trading-examples` repository.

**Independent Test**: Can be independently tested by running `./gradlew testDebugUnitTest --no-daemon` in `gh-development-dashboard` to verify that `architecture-registry.json`, `app/src/main/assets/architecture-registry.json`, `ArchitectureRegistry.LIVE_REPOSITORIES`, `ProjectRepository.seedLiveProjectsIfEmpty()`, `DashboardViewModel`, and `ArchitectureComplianceSection` include and render all 6 tracked repositories at `1.0.0` (`6/6 Up to Date`).

**Acceptance Scenarios**:

1. **Given** the canonical `architecture-registry.json` and `app/src/main/assets/architecture-registry.json` in `gh-development-dashboard`, **When** parsed by `ArchitectureRegistry`, **Then** `repo-examples/quant-trading-examples` is present as the 6th repository with `implemented = true`, `syncedVersion = "1.0.0"`, `lastUpdated = "2026-10-31"`, and its quantitative research stack description.
2. **Given** the dashboard is opened on a fresh installation or an installation missing any canonical registry repository, **When** `seedLiveProjectsIfEmpty()` runs, **Then** all 6 canonical repositories (including `repo-examples/quant-trading-examples`) are present in local storage without duplicating existing entries.
3. **Given** the user views the **Architecture & Way of Working Compliance** card on the dashboard, **When** expanded, **Then** it displays `6/6 Up to Date` and includes the row for `repo-examples/quant-trading-examples` (`quant-trading-examples`).

---

### Edge Cases

- **Missing `CLAUDE_CODE_OAUTH_TOKEN` in Private Repository (`repo-examples/quant-trading-examples`)**: If `CLAUDE_CODE_OAUTH_TOKEN` is not yet configured in the repository's GitHub Actions secrets, `.github/workflows/pr-review.yml` logs a clear `::notice::` and exits cleanly (`skip=true`) rather than failing the PR checks.
- **Auto-Fix Loop Prevention (`[skip pr-review]`)**: When Claude pushes an auto-fix commit containing `[skip pr-review]` alongside an `APPROVED` verdict, the subsequent `synchronize` run enters the zero-token `merge_only=true` fast path and waits for `CI` on the new commit SHA before squash-merging.
- **Upgrading Existing Dashboard Local Database from 5 to 6 Canonical Repositories**: When a device already has the original 5 `AntaresAndBharani` repositories stored in Room, `seedLiveProjectsIfEmpty()` ensures any missing canonical repository from `architectureRegistry.repositories` is inserted when empty or missing during canonical seeding without overwriting existing projects.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: `repo-examples/quant-trading-examples` MUST include `.github/workflows/ci.yml` (`name: CI`) triggered on `push` to `main` and `pull_request` targeting `main`, running Python 3.11+, installing dependencies (`pip install -e ".[dev]"`), running `ruff check src tests`, running `pytest -v`, and verifying `quantlab dashboard --check`.
- **FR-002**: `repo-examples/quant-trading-examples` MUST include `.github/workflows/claude.yml` (`name: Claude Code`) enabling interactive `@claude` mentions across issue comments, PR review comments, PR reviews, and issues using `anthropics/claude-code-action@v1` with `--model claude-opus-5-5 --effort high`.
- **FR-003**: `repo-examples/quant-trading-examples` MUST include `.github/workflows/pr-review.yml` (`name: PR Review`) implementing automated Claude Opus 5.5 High review, in-place auto-fix (`[skip pr-review]`), `cancel-in-progress: false` concurrency, zero-token `merge_only` fast-path recovery, 3-round escalation cap (`ROUND_CAP: 3`), self-healing label creation, exact-SHA `CI` workflow polling (`CI_WORKFLOW_NAME: "CI"`), squash-merge, and parent-story closure.
- **FR-004**: `repo-examples/quant-trading-examples` MUST include `.github/workflows/prompts/pr-review.md` tailored specifically to `quantlab`'s architecture (`src/quantlab/data/`, `src/quantlab/indicators/`, `src/quantlab/proofs/`, `src/quantlab/transpiler/`, `src/quantlab/dashboard/`, `src/quantlab/cli.py`, and `tests/`).
- **FR-005**: `repo-examples/quant-trading-examples` MUST include automated static contract tests (`tests/test_claude_workflows.py`) verifying the existence and required invariants of `.github/workflows/ci.yml`, `.github/workflows/claude.yml`, `.github/workflows/pr-review.yml`, and `.github/workflows/prompts/pr-review.md`.
- **FR-006**: `gh-development-dashboard` MUST update `architecture-registry.json`, `app/src/main/assets/architecture-registry.json`, and `ArchitectureRegistry.LIVE_REPOSITORIES` in `app/src/main/java/com/example/domain/ArchitectureModels.kt` to include `repo-examples/quant-trading-examples` (`owner = "repo-examples"`, `repo = "quant-trading-examples"`, `implemented = true`, `syncedVersion = "1.0.0"`, `lastUpdated = "2026-10-31"`), bringing the canonical tracked repository count to 6.
- **FR-007**: All unit and Robolectric UI tests in `gh-development-dashboard` (`ArchitectureComplianceTest.kt`, `ProjectRepositoryTest.kt`, `DashboardViewModelTest.kt`, `ArchitectureComplianceUiTest.kt`) MUST be updated and verified for the 6-repository canonical registry.

### Key Entities

- **RepoArchitectureEntry (`repo-examples/quant-trading-examples`)**: Canonical registry entry representing `repo-examples/quant-trading-examples` with `techStack = "Python 3.11 / Numba & SymPy / Streamlit & Pine v6"`, `implemented = true`, `syncedVersion = "1.0.0"`, and `lastUpdated = "2026-10-31"`.
- **QuantLab Workflow Suite**: The 4 standardized GitHub Actions files (`.github/workflows/ci.yml`, `.github/workflows/claude.yml`, `.github/workflows/pr-review.yml`, `.github/workflows/prompts/pr-review.md`) deployed in `repo-examples/quant-trading-examples`.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of the 6 tracked repositories (`5` in `AntaresAndBharani` + `repo-examples/quant-trading-examples`) have the v1.0.0 Claude Code Review workflow suite implemented and recorded as `UP_TO_DATE` (`6/6 Up to Date`) in `gh-development-dashboard`.
- **SC-002**: 100% of automated tests and linters in `repo-examples/quant-trading-examples` (`pytest -v` including `tests/test_claude_workflows.py`, and `ruff check src tests`) pass with zero errors.
- **SC-003**: 100% of unit and Robolectric Compose UI tests in `gh-development-dashboard` (`.\gradlew.bat testDebugUnitTest --no-daemon`) pass with zero errors.

## Assumptions

- `repo-examples/quant-trading-examples` uses `main` as its default branch and is accessible using the same GitHub token (`Set-GhToken-Antares.ps1`) as `AntaresAndBharani`.
- The Claude Code Review standard version remains `1.0.0` since `repo-examples/quant-trading-examples` is being onboarded onto the existing `1.0.0` workflow contract.
