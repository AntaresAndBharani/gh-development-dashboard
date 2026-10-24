# Phase 1 Data Model: Cross-Repository Architecture & Claude Code Review Compliance Tracking

## 1. Domain Models (`app/src/main/java/com/example/domain/ArchitectureModels.kt`)

### `ComplianceState` (Enum)
| Value | Display Label | Meaning |
|---|---|---|
| `UP_TO_DATE` | `"Up to Date"` | `.github/workflows/claude.yml` and `.github/workflows/pr-review.yml` are implemented AND `syncedVersion == targetVersion` in `ArchitectureRegistry`. |
| `OUTDATED` | `"Outdated"` | Workflow files are implemented, but `syncedVersion != targetVersion` or repo is absent from `ArchitectureRegistry`. |
| `NOT_IMPLEMENTED` | `"Not Implemented"` | One or both required workflow files are missing from the repository. |

### `ArchitecturePracticeSpec`
| Field | Type | Description |
|---|---|---|
| `id` | `String` | Unique identifier (e.g., `"claude-code-review"`) |
| `name` | `String` | Display name (`"Claude Code Review"`) |
| `description` | `String` | Summary of the architectural practice / way of working |
| `standardVersion` | `String` | Current target version across all repositories (`"1.0.0"`) |
| `lastUpdated` | `String` | ISO date of latest standard update (`"2026-10-24"`) |
| `requiredFiles` | `List<String>` | Required repository paths (`[".github/workflows/claude.yml", ".github/workflows/pr-review.yml", ".github/workflows/prompts/pr-review.md"]`) |

### `RepoArchitectureEntry`
| Field | Type | Description |
|---|---|---|
| `repoId` | `String` | Full repository identifier (`"owner/repo"`) |
| `owner` | `String` | GitHub organization/owner (`"AntaresAndBharani"`) |
| `repo` | `String` | Repository name (e.g., `"gh-development-dashboard"`) |
| `techStack` | `String` | Technology stack label (e.g., `"Android / Kotlin / Jetpack Compose"`) |
| `implemented` | `Boolean` | Baseline implementation flag in registry |
| `syncedVersion` | `String` | Version of the architecture standard synced in this repo (`"1.0.0"`) |
| `lastUpdated` | `String` | ISO date when this repository was updated (`"2026-10-24"`) |
| `notes` | `String` | Short notes on stack-specific workflow adaptation |

### `RepoComplianceStatus`
| Field | Type | Description |
|---|---|---|
| `repoId` | `String` | Full repository identifier (`"owner/repo"`) |
| `owner` | `String` | Repository owner |
| `repo` | `String` | Repository name |
| `techStack` | `String` | Technology stack descriptor (from registry or `"Custom / Unregistered"`) |
| `state` | `ComplianceState` | Computed compliance status (`UP_TO_DATE`, `OUTDATED`, `NOT_IMPLEMENTED`) |
| `isImplemented` | `Boolean` | True when Claude Code Review workflows are present |
| `isUpToDate` | `Boolean` | True when `state == ComplianceState.UP_TO_DATE` |
| `syncedVersion` | `String?` | Repository's recorded version in registry (`null` if unregistered) |
| `targetVersion` | `String` | Current target standard version (`"1.0.0"`) |
| `lastUpdated` | `String?` | Last rollout date from registry |
| `hasClaudeYml` | `Boolean` | Whether `.github/workflows/claude.yml` is present |
| `hasPrReviewYml` | `Boolean` | Whether `.github/workflows/pr-review.yml` is present |

### `ArchitectureComplianceSummary`
| Field | Type | Description |
|---|---|---|
| `practice` | `ArchitecturePracticeSpec` | Active architecture standard metadata |
| `repositories` | `List<RepoComplianceStatus>` | Sorted list of per-repository compliance statuses |
| `totalRepos` | `Int` | Total repositories evaluated |
| `implementedCount` | `Int` | Count of repositories where `isImplemented == true` |
| `upToDateCount` | `Int` | Count of repositories where `state == ComplianceState.UP_TO_DATE` |
| `outdatedCount` | `Int` | Count of repositories where `state == ComplianceState.OUTDATED` |
| `notImplementedCount` | `Int` | Count of repositories where `state == ComplianceState.NOT_IMPLEMENTED` |
| `compliancePercent` | `Int` | Percentage of repositories `UP_TO_DATE` (`0..100`) |

## 2. Extended `ProjectHealth` (`app/src/main/java/com/example/domain/Models.kt`)
- Add optional property `val complianceStatus: RepoComplianceStatus? = null` to `ProjectHealth` so each `ProjectCard` in `DashboardScreen` can directly render its Claude Code Review compliance badge and expanded details without breaking existing callers.

## 3. Remote DTO (`app/src/main/java/com/example/data/remote/Models.kt`)
- `RepoContentDto`:
  - `name: String`
  - `path: String`
  - `sha: String? = null`
  - `type: String? = null`
