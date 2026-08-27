package com.example.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.domain.ProjectHealth
import com.example.domain.SdlcStage
import com.example.domain.StageDwellTime
import com.example.domain.TimeWindow
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AnalyticsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleProjects = listOf(
        ProjectHealth(
            id = "owner/repo-alpha",
            owner = "owner",
            repo = "repo-alpha",
            userStories = emptyList(),
            pullRequests = emptyList()
        ),
        ProjectHealth(
            id = "owner/repo-beta",
            owner = "owner",
            repo = "repo-beta",
            userStories = emptyList(),
            pullRequests = emptyList()
        )
    )

    private val sampleStageDwellTimes = listOf(
        StageDwellTime(stage = SdlcStage.DEFINITION, durationMillis = 3600000L, transitionCount = 2),
        StageDwellTime(stage = SdlcStage.READY_FOR_ARCHITECT, durationMillis = 7200000L, transitionCount = 3),
        StageDwellTime(stage = SdlcStage.NEEDS_PO_INPUT, durationMillis = 1800000L, transitionCount = 1),
        StageDwellTime(stage = SdlcStage.REVIEW, durationMillis = 5400000L, transitionCount = 2),
        StageDwellTime(stage = SdlcStage.ARCHITECT_REWORK, durationMillis = 3600000L, transitionCount = 1),
        StageDwellTime(stage = SdlcStage.AWAITING_APPROVAL, durationMillis = 7200000L, transitionCount = 4),
        StageDwellTime(stage = SdlcStage.IN_PROGRESS, durationMillis = 28800000L, transitionCount = 5, isBottleneck = true),
        StageDwellTime(stage = SdlcStage.PR_REVIEW, durationMillis = 14400000L, transitionCount = 3),
        StageDwellTime(stage = SdlcStage.DONE, durationMillis = 0L, transitionCount = 2)
    )

    @Test
    fun `AnalyticsSection renders ScopeBar, TimeSelector, Total Dwell Time, and Chart`() {
        val uiState = AnalyticsUiState(
            selectedTimeWindow = TimeWindow.THREE_DAYS,
            selectedScope = null,
            stageDwellTimes = sampleStageDwellTimes,
            totalDwellTimeMillis = sampleStageDwellTimes.sumOf { it.durationMillis },
            bottleneckStage = SdlcStage.IN_PROGRESS
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                AnalyticsSection(
                    uiState = uiState,
                    projects = sampleProjects,
                    onTimeWindowSelected = {},
                    onScopeSelected = {}
                )
            }
        }

        // Verify main section tags
        composeTestRule.onNodeWithTag("analytics_section").assertIsDisplayed()
        composeTestRule.onNodeWithTag("scope_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("scope_chip_global").assertIsDisplayed()
        composeTestRule.onNodeWithTag("scope_chip_repo-alpha").assertIsDisplayed()
        composeTestRule.onNodeWithTag("scope_chip_repo-beta").assertIsDisplayed()
        composeTestRule.onNodeWithTag("time_range_selector").assertIsDisplayed()
        composeTestRule.onNodeWithTag("story_evolution_chart").assertIsDisplayed()
        composeTestRule.onNodeWithTag("story_evolution_canvas").assertIsDisplayed()

        // Bottleneck summary chip should be visible
        composeTestRule.onNodeWithTag("bottleneck_summary_chip").assertIsDisplayed()
    }

    @Test
    fun `TimeRangeSelector triggers onTimeWindowSelected when clicking 14D and 30D`() {
        var selectedWindow: TimeWindow? = null

        composeTestRule.setContent {
            MyApplicationTheme {
                TimeRangeSelector(
                    selectedWindow = TimeWindow.THREE_DAYS,
                    onWindowSelected = { selectedWindow = it }
                )
            }
        }

        composeTestRule.onNodeWithTag("time_window_TWO_WEEKS").performClick()
        assertEquals(TimeWindow.TWO_WEEKS, selectedWindow)

        composeTestRule.onNodeWithTag("time_window_ONE_MONTH").performClick()
        assertEquals(TimeWindow.ONE_MONTH, selectedWindow)

        composeTestRule.onNodeWithTag("time_window_THREE_DAYS").performClick()
        assertEquals(TimeWindow.THREE_DAYS, selectedWindow)
    }

    @Test
    fun `ScopeBar triggers onScopeSelected when clicking project chips and global chip`() {
        var selectedScope: String? = "dummy"

        composeTestRule.setContent {
            MyApplicationTheme {
                ScopeBar(
                    selectedScope = null,
                    projects = sampleProjects,
                    onScopeSelected = { selectedScope = it }
                )
            }
        }

        // Click repo-alpha chip
        composeTestRule.onNodeWithTag("scope_chip_repo-alpha").performClick()
        assertEquals("owner/repo-alpha", selectedScope)

        // Click Global chip
        composeTestRule.onNodeWithTag("scope_chip_global").performClick()
        assertNull(selectedScope)
    }

    @Test
    fun `StageDwellTimeCard displays bottleneck badge only when isBottleneck is true`() {
        val normalStage = StageDwellTime(
            stage = SdlcStage.DEFINITION,
            durationMillis = 3600000L,
            transitionCount = 2,
            isBottleneck = false
        )

        val bottleneckStage = StageDwellTime(
            stage = SdlcStage.IN_PROGRESS,
            durationMillis = 28800000L,
            transitionCount = 5,
            isBottleneck = true
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                StageDwellTimeCard(dwellTime = normalStage, isBottleneck = false)
                StageDwellTimeCard(dwellTime = bottleneckStage, isBottleneck = true)
            }
        }

        composeTestRule.onNodeWithTag("stage_card_DEFINITION").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottleneck_flag_DEFINITION").assertDoesNotExist()

        composeTestRule.onNodeWithTag("stage_card_IN_PROGRESS").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottleneck_flag_IN_PROGRESS").assertIsDisplayed()
    }

    @Test
    fun `StageDwellTimes are in strict 9-phase chronological order`() {
        assertEquals(9, SdlcStage.entries.size)
        val orderedStages = SdlcStage.entries.sortedBy { it.order }
        assertEquals(SdlcStage.DEFINITION, orderedStages[0])
        assertEquals(SdlcStage.READY_FOR_ARCHITECT, orderedStages[1])
        assertEquals(SdlcStage.NEEDS_PO_INPUT, orderedStages[2])
        assertEquals(SdlcStage.REVIEW, orderedStages[3])
        assertEquals(SdlcStage.ARCHITECT_REWORK, orderedStages[4])
        assertEquals(SdlcStage.AWAITING_APPROVAL, orderedStages[5])
        assertEquals(SdlcStage.IN_PROGRESS, orderedStages[6])
        assertEquals(SdlcStage.PR_REVIEW, orderedStages[7])
        assertEquals(SdlcStage.DONE, orderedStages[8])
    }
}
