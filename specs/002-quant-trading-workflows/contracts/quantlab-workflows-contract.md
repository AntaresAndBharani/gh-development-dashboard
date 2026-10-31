# Contract: `repo-examples/quant-trading-examples` GitHub Actions Workflows (Standard v1.0.0)

## 1. `.github/workflows/ci.yml`

- **Workflow Name**: `name: CI`
- **Triggers**:
  - `push.branches: [ main ]`
  - `pull_request.branches: [ main ]`
- **Steps**:
  1. `actions/checkout@v4`
  2. `actions/setup-python@v5` with `python-version: "3.11"` and `cache: "pip"`
  3. Install dependencies: `python -m pip install --upgrade pip && pip install -e ".[dev]"`
  4. Lint with Ruff: `ruff check src tests`
  5. Run pytest suite: `pytest -v`
  6. Run headless dashboard verification: `quantlab dashboard --check`

## 2. `.github/workflows/claude.yml`

- **Workflow Name**: `name: Claude Code`
- **Triggers**: `issue_comment` (`created`), `pull_request_review_comment` (`created`), `issues` (`opened`, `assigned`), `pull_request_review` (`submitted`)
- **Action**: `anthropics/claude-code-action@v1`
- **Arguments**: `--model claude-opus-5-5 --effort high`
- **Token**: `${{ secrets.CLAUDE_CODE_OAUTH_TOKEN }}`

## 3. `.github/workflows/pr-review.yml`

- **Workflow Name**: `name: PR Review`
- **Triggers**: `pull_request` (`opened`, `synchronize`, `ready_for_review`, `reopened` targeting `main`) and `workflow_dispatch` (`pr_number`)
- **Concurrency**: `group: pr-review-${{ github.event.pull_request.number || inputs.pr_number }}`, `cancel-in-progress: false`
- **Environment**:
  - `ROUND_CAP: 3`
  - `VERDICT_MARKER: "<!-- pr-review-verdict -->"`
  - `CI_WORKFLOW_NAME: "CI"`
- **Lifecycle**:
  1. Meta gate (`CLAUDE_CODE_OAUTH_TOKEN` notice skip if unconfigured, `[skip pr-review]` + `APPROVED` fast-path `merge_only=true`, 3-round escalation to `status:needs-po-input`).
  2. Checkout PR branch (`fetch-depth: 0`) and export `pr_meta.json` + `pr_diff.txt`.
  3. Execute `anthropics/claude-code-action@v1` (`--model claude-opus-5-5 --effort high`) with `.github/workflows/prompts/pr-review.md`.
  4. Stage, commit (`fix(pr-review): apply Claude Opus 5.5 auto-fixes [skip pr-review]`), and push any working-tree auto-fixes; post verdict comment; update labels (`review:approved` or `review:changes-requested` + `needs-refactor`).
  5. Poll `CI` workflow for `CURRENT_SHA` until `completed:success`, then squash-merge (`gh pr merge --squash --delete-branch`).
  6. Close linked parent story when all sibling sub-issues are closed.

## 4. `.github/workflows/prompts/pr-review.md`

- Tailored to `QuantLab` (`repo-examples/quant-trading-examples`) reviewing PIT causality, SymPy/Numba dual indicator parity, entropy/statistical/DSP proof gates, Pine Script v6 transpiler output, 4-quadrant diagnostic dashboard, and `pytest`/`ruff` compliance.
