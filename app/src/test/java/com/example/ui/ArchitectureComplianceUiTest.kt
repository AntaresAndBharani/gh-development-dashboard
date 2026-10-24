package com.example.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.domain.ArchitectureRegistry
import com.example.domain.ComplianceState
import com.example.domain.RepoComplianceStatus
import com.example.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ArchitectureComplianceUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `ArchitectureComplianceSection renders summary header, version chip, and expands to show 5 live repos`() {
        val summary = ArchitectureRegistry.defaultComplianceSummary()

        composeTestRule.setContent {
            MyApplicationTheme {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    ArchitectureComplianceSection(summary = summary)
                }
            }
        }

        composeTestRule.onNodeWithTag("architecture_compliance_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("architecture_version_chip", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("v1.0.0", substring = true, useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("architecture_summary_ratio", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("5/5 Up to Date", substring = true, useUnmergedTree = true).assertIsDisplayed()

        // Initially collapsed
        composeTestRule.onNodeWithTag("architecture_repo_row_crosstrainingapp").assertDoesNotExist()

        // Click header to expand
        composeTestRule.onNodeWithTag("architecture_compliance_toggle").performClick()

        composeTestRule.onNodeWithTag("architecture_repo_row_crosstrainingapp").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("architecture_repo_row_darwin-trader").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("architecture_repo_row_gh-development-dashboard").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("architecture_repo_row_kerberito").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("architecture_repo_row_retro-fighter").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `ProjectComplianceBadge renders UP_TO_DATE, OUTDATED, and NOT_IMPLEMENTED states`() {
        val upToDateStatus = RepoComplianceStatus(
            repoId = "AntaresAndBharani/gh-development-dashboard",
            owner = "AntaresAndBharani",
            repo = "gh-development-dashboard",
            techStack = "Kotlin / Jetpack Compose",
            state = ComplianceState.UP_TO_DATE,
            isImplemented = true,
            isUpToDate = true,
            syncedVersion = "1.0.0",
            targetVersion = "1.0.0",
            lastUpdated = "2026-10-24",
            hasClaudeYml = true,
            hasPrReviewYml = true
        )
        val outdatedStatus = upToDateStatus.copy(
            repoId = "AntaresAndBharani/legacy-repo",
            repo = "legacy-repo",
            state = ComplianceState.OUTDATED,
            isUpToDate = false,
            syncedVersion = "0.9.0"
        )
        val missingStatus = upToDateStatus.copy(
            repoId = "AntaresAndBharani/missing-repo",
            repo = "missing-repo",
            state = ComplianceState.NOT_IMPLEMENTED,
            isImplemented = false,
            isUpToDate = false,
            syncedVersion = null,
            hasClaudeYml = false,
            hasPrReviewYml = false
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                Column {
                    ProjectComplianceBadge(status = upToDateStatus)
                    ProjectComplianceBadge(status = outdatedStatus)
                    ProjectComplianceBadge(status = missingStatus)
                }
            }
        }

        composeTestRule.onNodeWithTag("project_compliance_badge_gh-development-dashboard", useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Up to Date · v1.0.0", substring = true, useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("project_compliance_badge_legacy-repo", useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Outdated", substring = true, useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("project_compliance_badge_missing-repo", useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Not Implemented", substring = true, useUnmergedTree = true)
            .assertIsDisplayed()
    }
}
