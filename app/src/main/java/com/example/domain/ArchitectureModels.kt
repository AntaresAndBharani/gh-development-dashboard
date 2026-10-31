package com.example.domain

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Compliance status for an organization-wide architectural practice (e.g. Claude Code Review).
 */
enum class ComplianceState(val displayName: String) {
    UP_TO_DATE("Up to Date"),
    OUTDATED("Outdated"),
    NOT_IMPLEMENTED("Not Implemented")
}

/**
 * Metadata describing a tracked architectural practice / way of working across repositories.
 */
@JsonClass(generateAdapter = true)
data class ArchitecturePracticeSpec(
    val practiceId: String = "claude-code-review",
    val practiceName: String = "Claude Code Review",
    val description: String = "Standardized AntaresAndBharani Claude Opus 5.5 High GitHub App architecture: interactive @claude mentions (claude.yml) and automated PR review, in-place auto-fix ([skip pr-review]), self-healing label governance, exact-SHA CI gating, squash-merge, and parent-story closure (pr-review.yml + prompts/pr-review.md).",
    val standardVersion: String = "1.0.0",
    val lastUpdated: String = "2026-10-31",
    val requiredFiles: List<String> = listOf(
        ".github/workflows/claude.yml",
        ".github/workflows/pr-review.yml",
        ".github/workflows/prompts/pr-review.md"
    )
)

/**
 * Canonical entry in the Architecture Registry for a known repository.
 */
@JsonClass(generateAdapter = true)
data class RepoArchitectureEntry(
    val repoId: String,
    val owner: String,
    val repo: String,
    val techStack: String,
    val implemented: Boolean,
    val syncedVersion: String,
    val lastUpdated: String,
    val notes: String = ""
)

/**
 * Root JSON document schema for `architecture-registry.json`.
 */
@JsonClass(generateAdapter = true)
data class ArchitectureRegistryDocument(
    val practiceId: String,
    val practiceName: String,
    val description: String,
    val standardVersion: String,
    val lastUpdated: String,
    val requiredFiles: List<String>,
    val repositories: List<RepoArchitectureEntry>
) {
    fun toPracticeSpec(): ArchitecturePracticeSpec = ArchitecturePracticeSpec(
        practiceId = practiceId,
        practiceName = practiceName,
        description = description,
        standardVersion = standardVersion,
        lastUpdated = lastUpdated,
        requiredFiles = requiredFiles
    )
}

/**
 * Evaluated compliance status for a single repository combining live GitHub workflow detection
 * and the canonical Architecture Registry stored in `gh-development-dashboard`.
 */
data class RepoComplianceStatus(
    val repoId: String,
    val owner: String,
    val repo: String,
    val techStack: String,
    val state: ComplianceState,
    val isImplemented: Boolean,
    val isUpToDate: Boolean,
    val syncedVersion: String?,
    val targetVersion: String,
    val lastUpdated: String?,
    val hasClaudeYml: Boolean,
    val hasPrReviewYml: Boolean,
    val notes: String = ""
)

/**
 * Aggregate compliance summary across all tracked repositories.
 */
data class ArchitectureComplianceSummary(
    val practice: ArchitecturePracticeSpec = ArchitectureRegistry.DEFAULT_PRACTICE,
    val repositories: List<RepoComplianceStatus> = emptyList(),
    val totalRepos: Int = 0,
    val implementedCount: Int = 0,
    val upToDateCount: Int = 0,
    val outdatedCount: Int = 0,
    val notImplementedCount: Int = 0,
    val compliancePercent: Int = 100
)

/**
 * Canonical Architecture Registry and pure evaluation functions.
 */
object ArchitectureRegistry {
    const val CLAUDE_WORKFLOW_PATH = ".github/workflows/claude.yml"
    const val PR_REVIEW_WORKFLOW_PATH = ".github/workflows/pr-review.yml"
    const val PR_REVIEW_PROMPT_PATH = ".github/workflows/prompts/pr-review.md"

    val DEFAULT_PRACTICE = ArchitecturePracticeSpec(
        practiceId = "claude-code-review",
        practiceName = "Claude Code Review",
        description = "Standardized AntaresAndBharani Claude Opus 5.5 High GitHub App architecture: interactive @claude mentions (claude.yml) and automated PR review, in-place auto-fix ([skip pr-review]), self-healing label governance, exact-SHA CI gating, squash-merge, and parent-story closure (pr-review.yml + prompts/pr-review.md).",
        standardVersion = "1.0.0",
        lastUpdated = "2026-10-31",
        requiredFiles = listOf(
            CLAUDE_WORKFLOW_PATH,
            PR_REVIEW_WORKFLOW_PATH,
            PR_REVIEW_PROMPT_PATH
        )
    )

    val LIVE_REPOSITORIES: List<RepoArchitectureEntry> = listOf(
        RepoArchitectureEntry(
            repoId = "AntaresAndBharani/crosstrainingapp",
            owner = "AntaresAndBharani",
            repo = "crosstrainingapp",
            techStack = "Android / Kotlin / Jetpack Compose",
            implemented = true,
            syncedVersion = "1.0.0",
            lastUpdated = "2026-10-23",
            notes = "Polls 'PR CI & Test Verification' on CURRENT_SHA; checks MVVM, Room migrations, Cloud Sync, and 360dp Compose UX."
        ),
        RepoArchitectureEntry(
            repoId = "AntaresAndBharani/retro-fighter",
            owner = "AntaresAndBharani",
            repo = "retro-fighter",
            techStack = "Godot 4.3 / GDScript / Python",
            implemented = true,
            syncedVersion = "1.0.0",
            lastUpdated = "2026-10-23",
            notes = "Polls 'CI' on CURRENT_SHA; runs lightweight Python unittest in-place and delegates Godot 4.3 headless verification to CI."
        ),
        RepoArchitectureEntry(
            repoId = "AntaresAndBharani/darwin-trader",
            owner = "AntaresAndBharani",
            repo = "darwin-trader",
            techStack = "Python FastAPI / React TypeScript",
            implemented = true,
            syncedVersion = "1.0.0",
            lastUpdated = "2026-10-23",
            notes = "Polls 'CI' on CURRENT_SHA; validates Python FastAPI backend, strategy engine, and React frontend."
        ),
        RepoArchitectureEntry(
            repoId = "AntaresAndBharani/gh-development-dashboard",
            owner = "AntaresAndBharani",
            repo = "gh-development-dashboard",
            techStack = "Android / Kotlin / Jetpack Compose",
            implemented = true,
            syncedVersion = "1.0.0",
            lastUpdated = "2026-10-24",
            notes = "Polls 'CI' on CURRENT_SHA; validates MVVM + UDF, Room DB, Architecture Registry parity, and Robolectric Compose tests."
        ),
        RepoArchitectureEntry(
            repoId = "AntaresAndBharani/kerberito",
            owner = "AntaresAndBharani",
            repo = "kerberito",
            techStack = "Python 3.11 / Textual TUI / Typer CLI",
            implemented = true,
            syncedVersion = "1.0.0",
            lastUpdated = "2026-10-24",
            notes = "Polls 'CI' on CURRENT_SHA; validates encrypted vault security, Typer CLI, Textual TUI, and pytest suite."
        ),
        RepoArchitectureEntry(
            repoId = "repo-examples/quant-trading-examples",
            owner = "repo-examples",
            repo = "quant-trading-examples",
            techStack = "Python 3.11 / Numba & SymPy / Streamlit & Pine v6",
            implemented = true,
            syncedVersion = "1.0.0",
            lastUpdated = "2026-10-31",
            notes = "Polls 'CI' on CURRENT_SHA; validates PIT causality, SymPy/Numba indicators, entropy/stats/DSP proofs, Pine v6 transpiler, and pytest/ruff."
        )
    )

    val DEFAULT_DOCUMENT = ArchitectureRegistryDocument(
        practiceId = DEFAULT_PRACTICE.practiceId,
        practiceName = DEFAULT_PRACTICE.practiceName,
        description = DEFAULT_PRACTICE.description,
        standardVersion = DEFAULT_PRACTICE.standardVersion,
        lastUpdated = DEFAULT_PRACTICE.lastUpdated,
        requiredFiles = DEFAULT_PRACTICE.requiredFiles,
        repositories = LIVE_REPOSITORIES
    )

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    fun parseRegistryJson(json: String): ArchitectureRegistryDocument {
        val adapter = moshi.adapter(ArchitectureRegistryDocument::class.java)
        return adapter.fromJson(json) ?: DEFAULT_DOCUMENT
    }

    fun findEntry(
        owner: String,
        repo: String,
        registry: ArchitectureRegistryDocument = DEFAULT_DOCUMENT
    ): RepoArchitectureEntry? {
        val id = "$owner/$repo"
        return registry.repositories.find {
            it.repoId.equals(id, ignoreCase = true) ||
                (it.owner.equals(owner, ignoreCase = true) && it.repo.equals(repo, ignoreCase = true))
        }
    }

    /**
     * Pure deterministic compliance evaluator combining live GitHub workflow detection
     * (`hasClaudeYml`, `hasPrReviewYml`) with the canonical `ArchitectureRegistryDocument`.
     *
     * When `hasClaudeYml` or `hasPrReviewYml` is null (e.g., offline or rate-limited API error),
     * falls back to `entry.implemented` in the registry (or `false` if unregistered).
     */
    fun evaluateRepoCompliance(
        owner: String,
        repo: String,
        hasClaudeYml: Boolean?,
        hasPrReviewYml: Boolean?,
        registry: ArchitectureRegistryDocument = DEFAULT_DOCUMENT
    ): RepoComplianceStatus {
        val entry = findEntry(owner, repo, registry)
        val baselineImplemented = entry?.implemented ?: false

        val resolvedHasClaude = hasClaudeYml ?: baselineImplemented
        val resolvedHasPrReview = hasPrReviewYml ?: baselineImplemented
        val isImplemented = resolvedHasClaude && resolvedHasPrReview

        val targetVersion = registry.standardVersion
        val syncedVersion = entry?.syncedVersion
        val isVersionCurrent = entry != null && entry.implemented && syncedVersion == targetVersion

        val state = when {
            !isImplemented -> ComplianceState.NOT_IMPLEMENTED
            isVersionCurrent -> ComplianceState.UP_TO_DATE
            else -> ComplianceState.OUTDATED
        }

        return RepoComplianceStatus(
            repoId = "$owner/$repo",
            owner = owner,
            repo = repo,
            techStack = entry?.techStack ?: "Custom / Unregistered",
            state = state,
            isImplemented = isImplemented,
            isUpToDate = state == ComplianceState.UP_TO_DATE,
            syncedVersion = syncedVersion,
            targetVersion = targetVersion,
            lastUpdated = entry?.lastUpdated,
            hasClaudeYml = resolvedHasClaude,
            hasPrReviewYml = resolvedHasPrReview,
            notes = entry?.notes ?: ""
        )
    }

    /**
     * Builds the aggregate `ArchitectureComplianceSummary` across all evaluated repositories.
     */
    fun buildArchitectureComplianceSummary(
        statuses: List<RepoComplianceStatus>,
        practice: ArchitecturePracticeSpec = DEFAULT_PRACTICE
    ): ArchitectureComplianceSummary {
        val sorted = statuses.sortedBy { it.repo.lowercase() }
        val total = sorted.size
        val implemented = sorted.count { it.isImplemented }
        val upToDate = sorted.count { it.state == ComplianceState.UP_TO_DATE }
        val outdated = sorted.count { it.state == ComplianceState.OUTDATED }
        val notImplemented = sorted.count { it.state == ComplianceState.NOT_IMPLEMENTED }
        val percent = if (total > 0) (upToDate * 100) / total else 100

        return ArchitectureComplianceSummary(
            practice = practice,
            repositories = sorted,
            totalRepos = total,
            implementedCount = implemented,
            upToDateCount = upToDate,
            outdatedCount = outdated,
            notImplementedCount = notImplemented,
            compliancePercent = percent
        )
    }

    /**
     * Builds the initial baseline compliance summary directly from the canonical registry.
     */
    fun defaultComplianceSummary(
        registry: ArchitectureRegistryDocument = DEFAULT_DOCUMENT
    ): ArchitectureComplianceSummary {
        val statuses = registry.repositories.map { entry ->
            evaluateRepoCompliance(
                owner = entry.owner,
                repo = entry.repo,
                hasClaudeYml = entry.implemented,
                hasPrReviewYml = entry.implemented,
                registry = registry
            )
        }
        return buildArchitectureComplianceSummary(statuses, registry.toPracticeSpec())
    }
}
