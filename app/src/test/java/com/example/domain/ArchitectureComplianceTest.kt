package com.example.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ArchitectureComplianceTest {

    private fun resolveRepoFile(relativePath: String): File {
        val direct = File(relativePath)
        if (direct.exists()) return direct
        val parent = File("..", relativePath)
        if (parent.exists()) return parent
        return direct
    }

    @Test
    fun `parseRegistryJson parses bundled architecture-registry_json with 6 live repos at version 1_0_0`() {
        val rootFile = resolveRepoFile("architecture-registry.json")
        val doc = ArchitectureRegistry.parseRegistryJson(rootFile.readText())
        assertEquals("claude-code-review", doc.practiceId)
        assertEquals("Claude Code Review", doc.practiceName)
        assertEquals("1.0.0", doc.standardVersion)
        assertEquals("2026-10-31", doc.lastUpdated)
        assertEquals(
            listOf(
                ".github/workflows/claude.yml",
                ".github/workflows/pr-review.yml",
                ".github/workflows/prompts/pr-review.md"
            ),
            doc.requiredFiles
        )
        assertEquals(6, doc.repositories.size)

        val expectedIds = listOf(
            "AntaresAndBharani/crosstrainingapp",
            "AntaresAndBharani/retro-fighter",
            "AntaresAndBharani/darwin-trader",
            "AntaresAndBharani/gh-development-dashboard",
            "AntaresAndBharani/kerberito",
            "repo-examples/quant-trading-examples"
        )
        assertEquals(expectedIds, doc.repositories.map { it.repoId })
        assertTrue(doc.repositories.all { it.implemented && it.syncedVersion == "1.0.0" })
    }

    @Test
    fun `root architecture-registry_json and assets architecture-registry_json are byte-for-byte identical`() {
        val rootFile = resolveRepoFile("architecture-registry.json")
        val assetFile = resolveRepoFile("app/src/main/assets/architecture-registry.json")

        assertTrue("Root architecture-registry.json must exist", rootFile.exists())
        assertTrue("Asset architecture-registry.json must exist", assetFile.exists())

        val rootText = rootFile.readText().replace("\r\n", "\n").trim()
        val assetText = assetFile.readText().replace("\r\n", "\n").trim()
        assertEquals(rootText, assetText)

        val parsed = ArchitectureRegistry.parseRegistryJson(rootText)
        assertEquals("1.0.0", parsed.standardVersion)
        assertEquals(6, parsed.repositories.size)
    }

    @Test
    fun `evaluateRepoCompliance returns UP_TO_DATE when both workflow files exist and version matches`() {
        val doc = ArchitectureRegistry.DEFAULT_DOCUMENT
        val status = ArchitectureRegistry.evaluateRepoCompliance(
            owner = "repo-examples",
            repo = "quant-trading-examples",
            hasClaudeYml = true,
            hasPrReviewYml = true,
            registry = doc
        )

        assertEquals(ComplianceState.UP_TO_DATE, status.state)
        assertTrue(status.isImplemented)
        assertTrue(status.isUpToDate)
        assertEquals("1.0.0", status.syncedVersion)
        assertEquals("1.0.0", status.targetVersion)
        assertTrue(status.hasClaudeYml)
        assertTrue(status.hasPrReviewYml)
    }

    @Test
    fun `evaluateRepoCompliance returns OUTDATED when workflows exist on GitHub but registered version is older or missing`() {
        val doc = ArchitectureRegistry.DEFAULT_DOCUMENT.copy(
            standardVersion = "2.0.0"
        )
        val status = ArchitectureRegistry.evaluateRepoCompliance(
            owner = "AntaresAndBharani",
            repo = "crosstrainingapp",
            hasClaudeYml = true,
            hasPrReviewYml = true,
            registry = doc
        )

        assertEquals(ComplianceState.OUTDATED, status.state)
        assertTrue(status.isImplemented)
        assertFalse(status.isUpToDate)
        assertEquals("1.0.0", status.syncedVersion)
        assertEquals("2.0.0", status.targetVersion)
    }

    @Test
    fun `evaluateRepoCompliance returns NOT_IMPLEMENTED when either claude_yml or pr-review_yml is missing`() {
        val doc = ArchitectureRegistry.DEFAULT_DOCUMENT
        val statusMissingPrReview = ArchitectureRegistry.evaluateRepoCompliance(
            owner = "AntaresAndBharani",
            repo = "kerberito",
            hasClaudeYml = true,
            hasPrReviewYml = false,
            registry = doc
        )

        assertEquals(ComplianceState.NOT_IMPLEMENTED, statusMissingPrReview.state)
        assertFalse(statusMissingPrReview.isImplemented)
        assertFalse(statusMissingPrReview.isUpToDate)
    }

    @Test
    fun `evaluateRepoCompliance falls back to registry entry when offline or rate-limited`() {
        val doc = ArchitectureRegistry.DEFAULT_DOCUMENT
        val offlineKnown = ArchitectureRegistry.evaluateRepoCompliance(
            owner = "repo-examples",
            repo = "quant-trading-examples",
            hasClaudeYml = null,
            hasPrReviewYml = null,
            registry = doc
        )

        assertEquals(ComplianceState.UP_TO_DATE, offlineKnown.state)
        assertTrue(offlineKnown.hasClaudeYml)
        assertTrue(offlineKnown.hasPrReviewYml)

        val offlineUnknown = ArchitectureRegistry.evaluateRepoCompliance(
            owner = "other",
            repo = "unregistered-repo",
            hasClaudeYml = null,
            hasPrReviewYml = null,
            registry = doc
        )
        assertEquals(ComplianceState.NOT_IMPLEMENTED, offlineUnknown.state)
        assertFalse(offlineUnknown.hasClaudeYml)
        assertFalse(offlineUnknown.hasPrReviewYml)
    }

    @Test
    fun `buildArchitectureComplianceSummary aggregates counts across 6 live repos plus extra repo`() {
        val defaultSummary = ArchitectureRegistry.defaultComplianceSummary()
        val extraStatus = RepoComplianceStatus(
            repoId = "custom/extra-repo",
            owner = "custom",
            repo = "extra-repo",
            techStack = "Custom",
            state = ComplianceState.NOT_IMPLEMENTED,
            isImplemented = false,
            isUpToDate = false,
            syncedVersion = null,
            targetVersion = "1.0.0",
            lastUpdated = null,
            hasClaudeYml = false,
            hasPrReviewYml = false
        )
        val summary = ArchitectureRegistry.buildArchitectureComplianceSummary(
            statuses = defaultSummary.repositories + extraStatus
        )

        assertEquals("1.0.0", summary.practice.standardVersion)
        assertEquals(7, summary.totalRepos)
        assertEquals(6, summary.upToDateCount)
        assertEquals(0, summary.outdatedCount)
        assertEquals(1, summary.notImplementedCount)
        assertEquals(6, summary.implementedCount)
    }

    @Test
    fun `gh-development-dashboard Claude Code Review and CI workflows satisfy standard v1_0_0 contract`() {
        val ciYml = resolveRepoFile(".github/workflows/ci.yml")
        val claudeYml = resolveRepoFile(".github/workflows/claude.yml")
        val prReviewYml = resolveRepoFile(".github/workflows/pr-review.yml")
        val prReviewPrompt = resolveRepoFile(".github/workflows/prompts/pr-review.md")
        val legacyMergeYml = resolveRepoFile(".github/workflows/merge.yml")

        assertTrue(".github/workflows/ci.yml must exist", ciYml.exists())
        assertTrue(".github/workflows/claude.yml must exist", claudeYml.exists())
        assertTrue(".github/workflows/pr-review.yml must exist", prReviewYml.exists())
        assertTrue(".github/workflows/prompts/pr-review.md must exist", prReviewPrompt.exists())
        assertFalse("Legacy .github/workflows/merge.yml must be removed", legacyMergeYml.exists())

        val ciText = ciYml.readText()
        assertTrue(ciText.contains("name: CI"))
        assertTrue(ciText.contains("testDebugUnitTest"))

        val claudeText = claudeYml.readText()
        assertTrue(claudeText.contains("name: Claude Code"))
        assertTrue(claudeText.contains("anthropics/claude-code-action@v1"))
        assertTrue(claudeText.contains("CLAUDE_CODE_OAUTH_TOKEN"))
        assertTrue(claudeText.contains("--model claude-opus-5-5 --effort high"))

        val prReviewText = prReviewYml.readText()
        assertTrue(prReviewText.contains("name: PR Review"))
        assertTrue(prReviewText.contains("ROUND_CAP: 3"))
        assertTrue(prReviewText.contains("<!-- pr-review-verdict -->"))
        assertTrue(prReviewText.contains("CI_WORKFLOW_NAME: \"CI\""))
        assertTrue(prReviewText.contains("[skip pr-review]"))
        assertTrue(prReviewText.contains("anthropics/claude-code-action@v1"))

        val promptText = prReviewPrompt.readText()
        assertTrue(promptText.contains("pr_review_output.json"))
        assertTrue(promptText.contains("APPROVED"))
        assertTrue(promptText.contains("CHANGES_REQUESTED"))
    }
}
