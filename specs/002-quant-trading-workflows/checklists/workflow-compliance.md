# Workflow & Architecture Compliance Checklist: Quant Trading Examples Workflow Rollout & Architecture Tracking

**Purpose**: Validate requirements quality for the `repo-examples/quant-trading-examples` workflow rollout and `gh-development-dashboard` 6-repository Architecture Registry integration
**Created**: 2026-10-31
**Feature**: [spec.md](../spec.md)

## Workflow Contract Completeness (`repo-examples/quant-trading-examples`)

- [x] CHK001 Are the exact workflow filenames (`.github/workflows/ci.yml`, `.github/workflows/claude.yml`, `.github/workflows/pr-review.yml`, `.github/workflows/prompts/pr-review.md`) explicitly specified in `spec.md` (`FR-001`..`FR-004`)?
- [x] CHK002 Does the specification define the exact `CI` workflow steps (`ruff check src tests`, `pytest -v`, `quantlab dashboard --check`) required for `repo-examples/quant-trading-examples` (`FR-001`)?
- [x] CHK003 Are the `pr-review.yml` concurrency, loop-cap (`ROUND_CAP: 3`), `[skip pr-review]` fast-path (`merge_only=true`), and exact-SHA `CI` polling requirements explicitly documented (`FR-003`)?
- [x] CHK004 Does the specification require domain-specific review instructions for `quantlab` (PIT causality, SymPy+Numba dual indicators, information-theoretic/statistical/DSP proof gates, Pine Script v6 transpiler, and Streamlit dashboard) in `.github/workflows/prompts/pr-review.md` (`FR-004`)?
- [x] CHK005 Are static workflow contract tests (`tests/test_claude_workflows.py`) explicitly required in `repo-examples/quant-trading-examples` (`FR-005`)?

## Architecture Registry & Dashboard Integration (`gh-development-dashboard`)

- [x] CHK006 Is the exact `repoId` (`repo-examples/quant-trading-examples`), `owner` (`repo-examples`), `repo` (`quant-trading-examples`), `syncedVersion` (`1.0.0`), and `lastUpdated` (`2026-10-31`) defined for the 6th repository (`FR-006`)?
- [x] CHK007 Does the specification require byte-for-byte synchronization between `architecture-registry.json`, `app/src/main/assets/architecture-registry.json`, and `ArchitectureRegistry.LIVE_REPOSITORIES` (`FR-006`)?
- [x] CHK008 Are edge cases for missing `CLAUDE_CODE_OAUTH_TOKEN` in private repositories and idempotent seeding of canonical repositories documented (`Edge Cases`)?
- [x] CHK009 Are unit and Robolectric Compose UI test updates (`ArchitectureComplianceTest.kt`, `ProjectRepositoryTest.kt`, `DashboardViewModelTest.kt`, `ArchitectureComplianceUiTest.kt`) explicitly required (`FR-007`, `SC-003`)?
- [x] CHK010 Are all measurable outcomes (`6/6 Up to Date`, 100% green `pytest` + `ruff` in `quant-trading-examples`, 100% green `testDebugUnitTest` in `gh-development-dashboard`) clearly quantified (`SC-001`..`SC-003`)?

## Notes

- All 10 requirement quality criteria (`CHK001`–`CHK010`) audited against `spec.md`, `plan.md`, `data-model.md`, and `contracts/quantlab-workflows-contract.md` and verified satisfied (`[x]`).
