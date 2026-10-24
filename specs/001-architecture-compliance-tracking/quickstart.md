# Quickstart & Validation Guide: Architecture & Claude Code Review Compliance Tracking

## Prerequisites
- JDK 17+ and Android SDK (API 36)
- Python 3.11+ with `pytest` (for `kerberito` verification)

## 1. Verify Canonical Architecture Registry
Confirm that `architecture-registry.json` and `app/src/main/assets/architecture-registry.json` exist and match, listing all 5 live `AntaresAndBharani` repositories (`crosstrainingapp`, `retro-fighter`, `darwin-trader`, `gh-development-dashboard`, `kerberito`) at `standardVersion = "1.0.0"`.

## 2. Run `gh-development-dashboard` Unit & UI Tests
From `c:\Users\rogal\workspaces\ws-setups\gh-development-dashboard`:
```powershell
.\gradlew.bat testDebugUnitTest --no-daemon
```
Expected outcome:
- `ArchitectureComplianceTest` verifies registry parsing, `UP_TO_DATE`, `OUTDATED`, `NOT_IMPLEMENTED`, and offline fallback evaluation, plus static contract checks on `.github/workflows/claude.yml`, `.github/workflows/pr-review.yml`, `.github/workflows/prompts/pr-review.md`, and `.github/workflows/ci.yml`.
- `ArchitectureComplianceUiTest` (Robolectric Compose test) verifies the expandable **"Architecture & Way of Working Compliance"** card and per-repository compliance badges on `ProjectCard`.
- `DashboardViewModelTest` and `ProjectRepositoryTest` verify live workflow detection, compliance state propagation, and default 5-repo seeding.

## 3. Run `kerberito` Test Suite
From `c:\Users\rogal\workspaces\ws-setups\kerberito`:
```powershell
pytest -v
```
Expected outcome:
- All existing vault, CLI, and TUI tests plus `tests/test_claude_workflows.py` pass 100%.
