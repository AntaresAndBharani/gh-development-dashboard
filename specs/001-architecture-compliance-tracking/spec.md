# Feature Specification: Cross-Repository Architecture & Claude Code Review Compliance Tracking

**Feature Branch**: `feat/001-architecture-compliance-tracking`

**Created**: 2026-10-24

**Status**: Approved

**Input**: User description: "I want to Add something to the dashboard. Antares and Bharani is using some architecture across the projects, so I want to start tracking if we implemented that architecture/way of working or not. So I want to start looking at if we implemented the claude code review in all the repositories and to know if the implementation is up to date. Due to the repositories have different technologies, is difficult to know if those repos are up to date, so we'll update all of them at the same time in this repository. So this repository must store some info about that. There are a few of them already updated: crosstraningapp, retro-fighter and darwin-trader. I want to update the rest of 'live repos for me' which are gh-development-dashboard and kerberito."

## Clarifications

### Session 2026-10-24

- Q: For updating the remaining live repositories (`gh-development-dashboard` and `kerberito`), what scope should we execute in this workflow? → A: Update both `gh-development-dashboard` and `kerberito` with the Claude Code Review workflows (`.github/workflows/claude.yml`, `pr-review.yml`, `prompts/pr-review.md`, and CI workflow if missing), plus add the Architecture/Claude Code Review tracking feature and registry in `gh-development-dashboard` so all 5 live repos are up to date.
- Q: How should `gh-development-dashboard` determine whether Claude Code Review is "Implemented" and "Up to Date" for each repository? → A: Combine live GitHub workflow detection with a versioned Architecture Registry stored in `gh-development-dashboard`: check via GitHub API whether `.github/workflows/claude.yml` and `.github/workflows/pr-review.yml` exist in each repo ("Implemented"), and check the stored Architecture Registry in this repo (which records the current standard version and each repo's synced version/date) to determine if it is "Up to Date".
- Q: How should the Architecture / Claude Code Review compliance status be displayed on the `DashboardScreen.kt` UI? → A: Show both an expandable "Architecture & Way of Working Compliance" card on the dashboard (summarizing all 5 live repos + any added repos, current standard version, Implemented vs Up-to-Date status) AND a Claude Code Review status badge/indicator directly on each repository's `ProjectCard`. Also pre-seed the 5 live `AntaresAndBharani` repos if not already in the database.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Dashboard Architecture & Way of Working Compliance Overview Card (Priority: P1)

As an engineering lead managing multiple repositories across heterogeneous technology stacks, I want a dedicated, expandable **"Architecture & Way of Working Compliance"** card on the main dashboard that displays the current architecture standard version, the aggregate compliance rate across all tracked repositories, and a per-repository breakdown showing whether **Claude Code Review** is **Implemented** and **Up to Date** (along with its synced version and rollout date).

**Why this priority**: Provides an immediate, centralized single-pane-of-glass view into whether all live projects follow the organization's standardized way of working and whether any repository lags behind the latest synchronized standard version.

**Independent Test**: Can be independently tested by launching the dashboard with the pre-seeded live repositories and verifying that the "Architecture & Way of Working Compliance" card displays the target standard version, aggregate compliance summary (e.g., `5/5 Up to Date`), and expands to list each repository's implementation status (`UP_TO_DATE`, `OUTDATED`, or `NOT_IMPLEMENTED`), synced version, and last rollout date.

**Acceptance Scenarios**:

1. **Given** the dashboard is opened for the first time with an empty project list, **When** initialization completes, **Then** the 5 live `AntaresAndBharani` repositories (`AntaresAndBharani/crosstrainingapp`, `AntaresAndBharani/retro-fighter`, `AntaresAndBharani/darwin-trader`, `AntaresAndBharani/gh-development-dashboard`, and `AntaresAndBharani/kerberito`) are automatically seeded and evaluated for Architecture & Way of Working compliance.
2. **Given** a tracked repository has both `.github/workflows/claude.yml` and `.github/workflows/pr-review.yml` detected (or confirmed in the baseline registry when offline) AND its recorded version in the Architecture Registry matches the current target standard version, **When** the user views the Architecture & Way of Working Compliance card, **Then** the repository is shown as **Implemented** and **Up to Date** with its version badge and rollout date.
3. **Given** a tracked repository has the required workflow files present on GitHub, **When** its recorded version in the Architecture Registry is older than the target standard version or absent from the registry, **Then** the repository is shown as **Implemented — Outdated** with an indicator showing the version gap.
4. **Given** a tracked repository does not have `.github/workflows/claude.yml` and `.github/workflows/pr-review.yml`, **When** the compliance check completes, **Then** the repository is shown as **Not Implemented**.

---

### User Story 2 - Per-Repository Architecture Compliance Badge on Project Cards (Priority: P1)

As a developer or engineering lead browsing individual repositories under **Active Repositories**, I want each repository card to display a clear **Claude Code Review** status badge (e.g., `Claude Review: Up to Date`, `Claude Review: Outdated`, or `Claude Review: Not Implemented`) and show the detailed architecture standard metadata when expanded.

**Why this priority**: Surfaces compliance status directly alongside each repository's stories, tasks, and pull requests so maintainers immediately spot unaligned repositories during daily triage.

**Independent Test**: Can be independently tested by rendering a repository card in each of the three compliance states (`UP_TO_DATE`, `OUTDATED`, `NOT_IMPLEMENTED`) and verifying the visible badge label, status indicator, and expanded architecture details.

**Acceptance Scenarios**:

1. **Given** a repository in the Active Repositories list whose Claude Code Review compliance status is `UP_TO_DATE`, **When** the repository card is rendered, **Then** a green compliance badge (`Claude Review: Up to Date · v1.0.0`) is displayed on the card header/summary.
2. **Given** a user adds a new external or unmigrated repository via the "Add GitHub Repository" dialog, **When** the repository's data loads, **Then** its workflow presence is checked against the Architecture Registry and its card displays `Claude Review: Outdated` (if workflows exist on GitHub but are not registered at the current standard version) or `Claude Review: Not Implemented` (if workflows are missing).

---

### User Story 3 - Synchronized Claude Code Review Rollout Across Remaining Live Repositories (`gh-development-dashboard` & `kerberito`) and Canonical Registry Storage (Priority: P1)

As the organization maintainer, because repositories use different technology stacks (Android/Kotlin, Godot/GDScript, Python/React, Python TUI/CLI) whose workflow files cannot be compared byte-for-byte, I want `gh-development-dashboard` to store the canonical versioned Architecture Registry tracking synchronized rollouts across all live repositories, and I want both `gh-development-dashboard` and `kerberito` updated with the full Claude Code Review workflow suite (`claude.yml`, `pr-review.yml`, `prompts/pr-review.md`, and `ci.yml`) so all 5 live repositories are at the latest standard version.

**Why this priority**: Establishes the canonical source of truth in `gh-development-dashboard` and brings 100% of the maintainer's live repositories up to the standardized Claude Code Review architecture.

**Independent Test**: Can be independently tested by verifying the canonical `architecture-registry.json` (and bundled asset) in `gh-development-dashboard` lists all 5 live repositories at standard version `1.0.0`, and by running static workflow contract verification against `.github/workflows/claude.yml`, `.github/workflows/pr-review.yml`, `.github/workflows/prompts/pr-review.md`, and `.github/workflows/ci.yml` in both `gh-development-dashboard` and `kerberito`.

**Acceptance Scenarios**:

1. **Given** the canonical Architecture Registry file stored in `gh-development-dashboard`, **When** inspected or loaded by the application, **Then** it defines the current standard version (`1.0.0`), the required workflow file paths (`.github/workflows/claude.yml`, `.github/workflows/pr-review.yml`, `.github/workflows/prompts/pr-review.md`), and entries for all 5 live repositories (`AntaresAndBharani/crosstrainingapp`, `AntaresAndBharani/retro-fighter`, `AntaresAndBharani/darwin-trader`, `AntaresAndBharani/gh-development-dashboard`, and `AntaresAndBharani/kerberito`) with their technology stack, synced standard version, and last updated date.
2. **Given** `gh-development-dashboard` and `kerberito`, **When** a user mentions `@claude` on an issue or pull request or opens/updates a non-draft pull request, **Then** `.github/workflows/claude.yml` and `.github/workflows/pr-review.yml` execute with Claude Opus 5.5 High, in-place auto-fix (`[skip pr-review]`), `cancel-in-progress: false`, zero-token `merge_only` fast-path recovery, self-healing label governance, exact-SHA `CI` workflow polling, squash-merge, and parent-story closure.

---

### Edge Cases

- **Offline or Rate-Limited GitHub API during Workflow Inspection**: If checking `.github/workflows/claude.yml` or `.github/workflows/pr-review.yml` via the GitHub API fails due to network unavailability or rate limiting (non-404 network error), the system falls back gracefully to the repository's recorded status in the bundled Architecture Registry so pre-registered live repositories still display accurate baseline compliance offline.
- **404 Not Found on Workflow File Endpoint**: If the GitHub API returns HTTP 404 for `.github/workflows/claude.yml` or `.github/workflows/pr-review.yml`, the capability is marked `NOT_IMPLEMENTED` even if the repository was previously listed in the registry, immediately alerting the maintainer to a removed or missing workflow file.
- **Unregistered Newly Added Repository**: If a user adds a 6th repository via the `+` button that is not in the bundled Architecture Registry:
  - If live GitHub detection finds both `claude.yml` and `pr-review.yml`, its status is `OUTDATED` (implemented, but not recorded in the latest synchronized registry rollout).
  - If live GitHub detection does not find both workflow files, its status is `NOT_IMPLEMENTED`.
- **Idempotent Seeding of Live Repositories**: Pre-seeding the 5 live `AntaresAndBharani` repositories on startup only seeds when the local projects table is empty (or inserts missing live repos without duplicating existing entries), preserving user-added repositories and avoiding duplicate refreshes.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The repository MUST store a canonical, versioned Architecture Registry (`architecture-registry.json` at the repository root and mirrored in `app/src/main/assets/architecture-registry.json`) defining the organization's tracked architecture standards (starting with `claude-code-review`), the current target standard version (`1.0.0`), the required repository file paths (`.github/workflows/claude.yml`, `.github/workflows/pr-review.yml`, `.github/workflows/prompts/pr-review.md`), and the synchronized status of each live repository.
- **FR-002**: The Architecture Registry MUST include all 5 live `AntaresAndBharani` repositories (`AntaresAndBharani/crosstrainingapp`, `AntaresAndBharani/retro-fighter`, `AntaresAndBharani/darwin-trader`, `AntaresAndBharani/gh-development-dashboard`, and `AntaresAndBharani/kerberito`) along with their technology stack descriptor, implemented flag, synced standard version (`1.0.0`), and rollout date (`2026-10-24`).
- **FR-003**: When the application starts with no persisted repositories in local storage, the system MUST automatically seed the 5 live repositories from the Architecture Registry so all 5 live repositories are tracked out of the box.
- **FR-004**: For each tracked repository, the system MUST check whether `.github/workflows/claude.yml` and `.github/workflows/pr-review.yml` exist in the remote repository and combine that signal with the Architecture Registry entry to compute a deterministic compliance status:
  - `UP_TO_DATE`: Workflow files are implemented AND the repository's registered version in the Architecture Registry matches the target standard version (`1.0.0`).
  - `OUTDATED`: Workflow files are implemented, but the repository is either absent from the Architecture Registry or registered with a version different from the target standard version.
  - `NOT_IMPLEMENTED`: Required workflow files are missing from the repository.
- **FR-005**: If remote workflow file inspection fails due to transient network unavailability or rate limiting (as opposed to an explicit 404 Not Found), the system MUST fall back to the repository's baseline entry in the Architecture Registry so offline usage remains functional.
- **FR-006**: The dashboard UI MUST display an expandable **"Architecture & Way of Working Compliance"** card showing:
  - The tracked practice name (`Claude Code Review`) and current standard version (`v1.0.0`).
  - The aggregate compliance ratio across tracked repositories (e.g., `5/5 Up to Date`, count of `Implemented`, `Outdated`, and `Not Implemented`).
  - An expandable per-repository compliance list showing each repository's name, tech stack, implementation state (`Implemented` vs `Not Implemented`), currency state (`Up to Date`, `Outdated`, `Not Implemented`), synced version, and last rollout date.
- **FR-007**: Each repository's `ProjectCard` in the **Active Repositories** section MUST display a visible compliance badge indicating its Claude Code Review status (`Up to Date`, `Outdated`, or `Not Implemented`) and version, and display the architecture compliance summary inside the expanded card view.
- **FR-008**: `gh-development-dashboard` MUST implement `.github/workflows/ci.yml` (`name: CI`) running `./gradlew testDebugUnitTest --no-daemon` on `push` to `main` and `pull_request` targeting `main`, providing the commit-scoped CI check required by the automated PR review pipeline.
- **FR-009**: `gh-development-dashboard` MUST implement `.github/workflows/claude.yml`, `.github/workflows/pr-review.yml`, and `.github/workflows/prompts/pr-review.md` (replacing the legacy label-only `.github/workflows/merge.yml` so merges are gated by Claude Opus 5.5 High review and exact-SHA `CI` verification).
- **FR-010**: `kerberito` MUST be updated with `.github/workflows/claude.yml`, `.github/workflows/pr-review.yml`, `.github/workflows/prompts/pr-review.md`, and static workflow contract tests (`tests/test_claude_workflows.py`) aligned with its existing `.github/workflows/ci.yml` (`name: CI`).

### Key Entities

- **ArchitectureStandard**: Represents an organization-wide architectural practice or way of working (id `claude-code-review`, display name `Claude Code Review`, current target version `1.0.0`, last updated date, required file paths).
- **RepoArchitectureEntry**: Represents a repository's recorded compliance entry in the canonical Architecture Registry (`repoId` as `owner/repo`, `techStack`, `implemented`, `syncedVersion`, `lastUpdated`).
- **RepoComplianceStatus**: Computed domain model combining live GitHub workflow detection with the `RepoArchitectureEntry` (`repoId`, `techStack`, `isImplemented`, `isUpToDate`, `state` enum `[UP_TO_DATE, OUTDATED, NOT_IMPLEMENTED]`, `syncedVersion`, `targetVersion`, `lastUpdated`, `hasClaudeYml`, `hasPrReviewYml`).
- **ArchitectureComplianceSummary**: Aggregate domain model exposed in `DashboardViewModel` summarizing the standard version, total tracked repos, implemented count, up-to-date count, outdated count, not-implemented count, and per-repository `RepoComplianceStatus` list.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of the 5 live `AntaresAndBharani` repositories (`crosstrainingapp`, `retro-fighter`, `darwin-trader`, `gh-development-dashboard`, `kerberito`) have the standardized Claude Code Review workflows (`claude.yml`, `pr-review.yml`, `prompts/pr-review.md`, and `ci.yml` / build verification) implemented and recorded at standard version `1.0.0` in the canonical Architecture Registry.
- **SC-002**: Maintainers can view both the aggregate Architecture & Way of Working compliance rate (`5/5 Up to Date`) and any individual repository's `Implemented` and `Up to Date` status within 1 tap on the dashboard screen.
- **SC-003**: 100% of compliance state permutations (`UP_TO_DATE`, `OUTDATED`, `NOT_IMPLEMENTED`, and offline registry fallback) are deterministically evaluated and verified by automated unit and UI tests.
- **SC-004**: 100% of automated test suites in `gh-development-dashboard` (`./gradlew testDebugUnitTest --no-daemon`) and `kerberito` (`pytest -v`) pass with zero failures.

## Assumptions

- All 5 live repositories belong to the `AntaresAndBharani` GitHub organization (`AntaresAndBharani/crosstrainingapp`, `AntaresAndBharani/retro-fighter`, `AntaresAndBharani/darwin-trader`, `AntaresAndBharani/gh-development-dashboard`, `AntaresAndBharani/kerberito`).
- Synchronized updates across heterogeneous repositories are tracked by bumping `standardVersion` in `architecture-registry.json` (and `app/src/main/assets/architecture-registry.json`) and updating each migrated repository's `syncedVersion` and `lastUpdated` fields in this repository.
- GitHub repository contents endpoint (`GET /repos/{owner}/{repo}/contents/{path}`) is accessible using the same GitHub authentication configuration already used by `RetrofitClient` in `gh-development-dashboard`.
