# Phase 0 Research: Cross-Repository Architecture & Claude Code Review Compliance Tracking

## 1. Canonical Architecture Registry Storage & Synchronization Across Heterogeneous Tech Stacks

- **Decision**: Store a canonical JSON manifest at `architecture-registry.json` in the root of `gh-development-dashboard` and mirror it into `app/src/main/assets/architecture-registry.json` (plus a deterministic Kotlin fallback/parser in `com.example.domain.ArchitectureRegistry`).
- **Rationale**:
  - The 5 live repositories use distinct technology stacks:
    1. `AntaresAndBharani/crosstrainingapp` (Android / Kotlin / Jetpack Compose)
    2. `AntaresAndBharani/retro-fighter` (Godot 4.3 / GDScript / Python)
    3. `AntaresAndBharani/darwin-trader` (Python FastAPI / React TypeScript)
    4. `AntaresAndBharani/gh-development-dashboard` (Android / Kotlin / Jetpack Compose)
    5. `AntaresAndBharani/kerberito` (Python 3.11 / Textual TUI / Typer CLI)
  - Because each repository's `.github/workflows/pr-review.yml` and `.github/workflows/prompts/pr-review.md` contain stack-specific build/test/review instructions, raw file hashes cannot be compared across repositories to determine if an implementation is up to date.
  - Maintaining the versioned `architecture-registry.json` in `gh-development-dashboard` allows the maintainer to update all live repositories simultaneously and record the synchronized `standardVersion` (`1.0.0`) and `lastUpdated` date (`2026-10-24`) in one authoritative place.
- **Alternatives Considered**:
  - *Comparing SHA-256 hashes of `.github/workflows/pr-review.yml` across repos*: Rejected because technology-specific steps (`CI_WORKFLOW_NAME`, prompt files, test runners) legitimately differ per repo.
  - *Hardcoding only in Kotlin without a root JSON file*: Rejected because a root `architecture-registry.json` file is human-editable, diffable in PRs, and verifiable by static CI checks.

## 2. Live GitHub Workflow Detection + Registry Evaluation Logic

- **Decision**: Add `getRepoFileContent(owner, repo, path)` (`@GET("repos/{owner}/{repo}/contents/{path}")`) to `GitHubService` returning `RepoContentDto`. Check `.github/workflows/claude.yml` and `.github/workflows/pr-review.yml` for each repository, and combine with `ArchitectureRegistry` to compute `ComplianceState`:
  - `UP_TO_DATE`: Both workflow files exist (`isImplemented = true`) AND the repository entry in `ArchitectureRegistry` has `syncedVersion == standardVersion` (`1.0.0`).
  - `OUTDATED`: Both workflow files exist (`isImplemented = true`), but the repository is either not listed in `ArchitectureRegistry` or its `syncedVersion != standardVersion`.
  - `NOT_IMPLEMENTED`: Either `.github/workflows/claude.yml` or `.github/workflows/pr-review.yml` is missing (HTTP 404 or `implemented = false` when offline).
- **Offline / Rate-Limit Resilience**:
  - If `getRepoFileContent` throws a ` retrofit2.HttpException` with code `404` (or a message containing `"404"`), the file is definitively missing (`false`).
  - If `getRepoFileContent` throws any other network/IO exception (offline, timeout, 403 rate limit), the repository falls back to its baseline `implemented` flag in `ArchitectureRegistry`.
- **Rationale**: Combines real-time GitHub truth (detecting newly added or accidentally deleted workflow files) with authoritative cross-stack version currency from the local registry, while remaining 100% functional offline.
- **Alternatives Considered**:
  - *Registry-only without GitHub API check*: Rejected because user selected live GitHub workflow detection combined with the stored Architecture Registry.

## 3. Claude Code Review Rollout to `gh-development-dashboard` and `kerberito`

- **Decision**:
  - For `gh-development-dashboard`:
    1. Create `.github/workflows/ci.yml` (`name: CI`) triggered on `push` (`main`) and `pull_request` (`main`), running `./gradlew testDebugUnitTest --no-daemon`.
    2. Create `.github/workflows/claude.yml` for interactive `@claude` mentions using `anthropics/claude-code-action@v1` with `--model claude-opus-5-5 --effort high`.
    3. Create `.github/workflows/pr-review.yml` (`CI_WORKFLOW_NAME: "CI"`) and `.github/workflows/prompts/pr-review.md` tailored to `gh-development-dashboard` (MVVM + UDF, Room DB entities/DAOs, Architecture Registry parity between root `architecture-registry.json` and `app/src/main/assets/architecture-registry.json`, Compose `testTag` coverage, and `gradle.properties` isolation).
    4. Remove legacy `.github/workflows/merge.yml` so merges are unified inside `pr-review.yml` after exact-SHA `CI` verification.
  - For `kerberito` (`c:\Users\rogal\workspaces\ws-setups\kerberito`):
    1. Preserve existing `.github/workflows/ci.yml` (`name: CI`).
    2. Create `.github/workflows/claude.yml` for interactive `@claude` mentions.
    3. Create `.github/workflows/pr-review.yml` (`CI_WORKFLOW_NAME: "CI"`) and `.github/workflows/prompts/pr-review.md` tailored to `kerberito` (Python 3.11, cryptography vault security, Textual TUI & Typer CLI architecture, `pytest -v` verification).
    4. Add `tests/test_claude_workflows.py` in `kerberito` to verify workflow and prompt contracts under `pytest -v`.
- **Rationale**: Brings all 5 live repositories (`crosstrainingapp`, `retro-fighter`, `darwin-trader`, `gh-development-dashboard`, `kerberito`) to the exact same `v1.0.0` Claude Code Review standard.
