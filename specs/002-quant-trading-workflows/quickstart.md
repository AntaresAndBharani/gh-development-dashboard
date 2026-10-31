# Quickstart & Validation Guide: Quant Trading Examples Workflow Rollout & Architecture Tracking

## 1. Verify Workflows & Tests in `repo-examples/quant-trading-examples`

```powershell
Push-Location "C:\Users\rogal\workspaces\ws-trading\quant-trading-examples"
uv run pytest -v
uv run ruff check src tests
uv run quantlab dashboard --check
Pop-Location
```

**Expected Outcome**:
- `tests/test_claude_workflows.py` passes alongside all existing `quantlab` test suites (`37 passed`).
- `ruff check src tests` reports `All checks passed!`.
- `quantlab dashboard --check` exits with code `0`.

## 2. Verify Registry & Unit/UI Tests in `gh-development-dashboard`

```powershell
Push-Location "C:\Users\rogal\workspaces\ws-setups\gh-development-dashboard"
.\gradlew.bat testDebugUnitTest --no-daemon
Pop-Location
```

**Expected Outcome**:
- `ArchitectureComplianceTest` verifies `architecture-registry.json` and `app/src/main/assets/architecture-registry.json` are byte-for-byte identical and contain all 6 tracked repositories (`5` in `AntaresAndBharani` + `repo-examples/quant-trading-examples`) at `standardVersion = "1.0.0"`.
- `ProjectRepositoryTest`, `DashboardViewModelTest`, and `ArchitectureComplianceUiTest` pass with `6/6 Up to Date`.
