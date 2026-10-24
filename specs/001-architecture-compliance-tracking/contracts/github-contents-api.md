# Contract: GitHub Repository Contents Inspection & Compliance Evaluation

## 1. GitHub REST API Endpoint (`GitHubService.getRepoFileContent`)

```http
GET /repos/{owner}/{repo}/contents/{path}
```

### Path Parameters
- `owner`: GitHub organization/user (e.g., `AntaresAndBharani`)
- `repo`: Repository name (e.g., `gh-development-dashboard`)
- `path`: Unencoded relative path (`@Path(value = "path", encoded = true)`), tested against:
  - `.github/workflows/claude.yml`
  - `.github/workflows/pr-review.yml`

### Response (`200 OK`)
```json
{
  "name": "claude.yml",
  "path": ".github/workflows/claude.yml",
  "sha": "e69de29bb2d1d6434b8b29ae775ad8c2e48c5391",
  "type": "file"
}
```

### Error Handling & Compliance State Truth Table

| Remote `.github/workflows/claude.yml` | Remote `.github/workflows/pr-review.yml` | Registry Entry (`architecture-registry.json`) | Resulting `ComplianceState` |
|---|---|---|---|
| `200 OK` | `200 OK` | Present & `syncedVersion == standardVersion` | `UP_TO_DATE` |
| `200 OK` | `200 OK` | Present & `syncedVersion != standardVersion` | `OUTDATED` |
| `200 OK` | `200 OK` | Absent (unregistered repo) | `OUTDATED` |
| `404 Not Found` | Any | Any | `NOT_IMPLEMENTED` |
| Any | `404 Not Found` | Any | `NOT_IMPLEMENTED` |
| Network / Rate-Limit Error | Network / Rate-Limit Error | Present (`implemented = true`, `syncedVersion == standardVersion`) | `UP_TO_DATE` (Offline Fallback) |
| Network / Rate-Limit Error | Network / Rate-Limit Error | Absent | `NOT_IMPLEMENTED` |
