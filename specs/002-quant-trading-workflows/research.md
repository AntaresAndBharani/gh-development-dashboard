# Phase 0 Research: Quant Trading Examples Workflow Rollout & Architecture Tracking

## 1. CI Workflow Design for `repo-examples/quant-trading-examples` (`quantlab`)

- **Decision**: Create `.github/workflows/ci.yml` (`name: CI`) running on `ubuntu-latest` with Python 3.11 (`actions/setup-python@v5`), installing `pip install -e ".[dev]"`, executing `ruff check src tests`, running `pytest -v`, and verifying `quantlab dashboard --check`.
- **Rationale**:
  - `pr-review.yml` polls `CI_WORKFLOW_NAME: "CI"` on the exact commit SHA (`CURRENT_SHA`) before squash-merging.
  - `quantlab` (`pyproject.toml`) specifies `requires-python = ">=3.11"`, `[project.optional-dependencies] dev = ["pytest>=8.2.0", "ruff>=0.4.0"]`, and entry point `quantlab = "quantlab.cli:app"`.
  - Running `ruff check src tests`, `pytest -v`, and `quantlab dashboard --check` validates lint compliance, all 35+ unit/mathematical/transpiler tests, and the 4-quadrant diagnostic dashboard headless check.
- **Alternatives Considered**:
  - *Only running `pytest -v` without `ruff`*: Rejected because `README.md` in `quant-trading-examples` mandates both `pytest -v` and `ruff check src tests`.

## 2. Tailoring `.github/workflows/prompts/pr-review.md` for `quantlab`

- **Decision**: Tailor `.github/workflows/prompts/pr-review.md` in `repo-examples/quant-trading-examples` to inspect `quantlab`'s specific domain invariants:
  1. **Point-in-Time (PIT) Causal Data & Stationarity** (`src/quantlab/data/loader.py`, `src/quantlab/data/stationarity.py`): Zero lookahead bias, causal fixed-width window fractional differentiation (`FracDiff`), and ADF stationarity gates.
  2. **Dual Neuro-Symbolic Indicator Registry** (`src/quantlab/indicators/registry.py`, `baselines.py`, `custom_examples.py`): Parity between `@numba.njit` vectorized kernels and canonical `sympy.Expr` AST representations.
  3. **Information-Theoretic, Statistical & DSP Proof Gates** (`src/quantlab/proofs/entropy.py`, `stats_gate.py`, `dsp.py`): KSG Mutual Information, Causal Transfer Entropy with IAAFT/block-shuffle surrogates, Spearman Rank IC, Wasserstein regime distance, VIF orthogonality, and Hilbert transform / Bode / Jerk Pareto frontier soundness.
  4. **SymPy-to-Pine Script v6 Transpiler & 4-Quadrant Dashboard** (`src/quantlab/transpiler/pine_v6.py`, `src/quantlab/dashboard/app.py`, `src/quantlab/cli.py`): Valid `//@version=6` emission, Markdown Research Card generation, and Streamlit/Plotly/Lightweight Charts headless diagnostic checks.
  5. **Test Parity & Lint Hygiene** (`tests/test_*.py`, `ruff check src tests`, `CHANGELOG.md`).
- **Rationale**: Matches the domain-specific review rigor of `kerberito`, `darwin-trader`, `crosstrainingapp`, `retro-fighter`, and `gh-development-dashboard` while maintaining the identical `pr_review_output.json` schema required by `pr-review.yml`.
- **Alternatives Considered**:
  - *Generic boilerplate prompt*: Rejected because the user explicitly requested workflows adapted to the repository.

## 3. Canonical Registry & Seeding Update in `gh-development-dashboard`

- **Decision**:
  - Add `repo-examples/quant-trading-examples` as the 6th entry in `architecture-registry.json`, `app/src/main/assets/architecture-registry.json`, and `ArchitectureRegistry.LIVE_REPOSITORIES` (`app/src/main/java/com/example/domain/ArchitectureModels.kt`).
  - Keep `standardVersion = "1.0.0"` and update `lastUpdated = "2026-10-31"` so all 6 tracked repositories evaluate to `ComplianceState.UP_TO_DATE` (`6/6 Up to Date`).
  - Update `ProjectRepository.seedLiveProjectsIfEmpty()` so that when `autoSeedLiveProjects` triggers on startup, any missing canonical repository from `architectureRegistry.repositories` is inserted idempotently (working both on a fresh empty database and when upgrading from 5 to 6 canonical repositories).
- **Rationale**: Ensures both new and existing installations of `gh-development-dashboard` immediately surface `repo-examples/quant-trading-examples` in the compliance summary and project list.
- **Alternatives Considered**:
  - *Bumping `standardVersion` to `1.1.0`*: Rejected because the workflow contract (`claude.yml`, `pr-review.yml`, `prompts/pr-review.md`) is unchanged at `1.0.0`, and bumping `standardVersion` would artificially mark the other 5 repositories as `OUTDATED`.
