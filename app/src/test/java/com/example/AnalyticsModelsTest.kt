package com.example

import com.example.domain.DwellTimeSummary
import com.example.domain.SdlcStage
import com.example.domain.StageDwellTime
import com.example.domain.StageTransition
import com.example.domain.TimeWindow
import com.example.domain.calculateDwellTimeSummary
import com.example.domain.calculateStageDwellTimes
import com.example.domain.formatDuration
import com.example.domain.parseTimestampToMillis
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class AnalyticsModelsTest {

    @Test
    fun sdlcStage_fromLabel_mapsAllNinePhasesCorrectly() {
        assertEquals(SdlcStage.DEFINITION, SdlcStage.fromLabel("status:definition"))
        assertEquals(SdlcStage.DEFINITION, SdlcStage.fromLabel("definition"))
        assertEquals(SdlcStage.DEFINITION, SdlcStage.fromLabel("drafting"))

        assertEquals(SdlcStage.READY_FOR_ARCHITECT, SdlcStage.fromLabel("status:ready-for-architect"))
        assertEquals(SdlcStage.READY_FOR_ARCHITECT, SdlcStage.fromLabel("ready-for-architect"))

        assertEquals(SdlcStage.NEEDS_PO_INPUT, SdlcStage.fromLabel("status:needs-po-input"))
        assertEquals(SdlcStage.NEEDS_PO_INPUT, SdlcStage.fromLabel("needs-po-input"))

        assertEquals(SdlcStage.REVIEW, SdlcStage.fromLabel("status:review"))
        assertEquals(SdlcStage.REVIEW, SdlcStage.fromLabel("review"))
        assertEquals(SdlcStage.REVIEW, SdlcStage.fromLabel("status:in-review"))

        assertEquals(SdlcStage.ARCHITECT_REWORK, SdlcStage.fromLabel("status:needs-revision"))
        assertEquals(SdlcStage.ARCHITECT_REWORK, SdlcStage.fromLabel("needs-revision"))
        assertEquals(SdlcStage.ARCHITECT_REWORK, SdlcStage.fromLabel("status:needs-clarification"))
        assertEquals(SdlcStage.ARCHITECT_REWORK, SdlcStage.fromLabel("needs-clarification"))

        assertEquals(SdlcStage.AWAITING_APPROVAL, SdlcStage.fromLabel("status:awaiting-approval"))
        assertEquals(SdlcStage.AWAITING_APPROVAL, SdlcStage.fromLabel("awaiting-approval"))
        assertEquals(SdlcStage.AWAITING_APPROVAL, SdlcStage.fromLabel("status:ready"))
        assertEquals(SdlcStage.AWAITING_APPROVAL, SdlcStage.fromLabel("ready"))

        assertEquals(SdlcStage.IN_PROGRESS, SdlcStage.fromLabel("status:in-progress"))
        assertEquals(SdlcStage.IN_PROGRESS, SdlcStage.fromLabel("in-progress"))
        assertEquals(SdlcStage.IN_PROGRESS, SdlcStage.fromLabel("status:in-development"))

        assertEquals(SdlcStage.PR_REVIEW, SdlcStage.fromLabel("review:changes-requested"))
        assertEquals(SdlcStage.PR_REVIEW, SdlcStage.fromLabel("review:approved"))
        assertEquals(SdlcStage.PR_REVIEW, SdlcStage.fromLabel("changes-requested"))
        assertEquals(SdlcStage.PR_REVIEW, SdlcStage.fromLabel("approved"))

        assertEquals(SdlcStage.DONE, SdlcStage.fromLabel("status:done"))
        assertEquals(SdlcStage.DONE, SdlcStage.fromLabel("done"))
        assertEquals(SdlcStage.DONE, SdlcStage.fromLabel("closed"))
        assertEquals(SdlcStage.DONE, SdlcStage.fromLabel("status:closed"))

        assertNull(SdlcStage.fromLabel("unknown-label"))
        assertNull(SdlcStage.fromLabel("bug"))
    }

    @Test
    fun calculateStageDwellTimes_alwaysReturnsExactNinePhasesInStrictOrder() {
        val result = calculateStageDwellTimes(
            transitions = emptyList(),
            timeWindow = TimeWindow.ONE_MONTH
        )

        assertEquals(9, result.size)
        assertEquals(SdlcStage.DEFINITION, result[0].stage)
        assertEquals(SdlcStage.READY_FOR_ARCHITECT, result[1].stage)
        assertEquals(SdlcStage.NEEDS_PO_INPUT, result[2].stage)
        assertEquals(SdlcStage.REVIEW, result[3].stage)
        assertEquals(SdlcStage.ARCHITECT_REWORK, result[4].stage)
        assertEquals(SdlcStage.AWAITING_APPROVAL, result[5].stage)
        assertEquals(SdlcStage.IN_PROGRESS, result[6].stage)
        assertEquals(SdlcStage.PR_REVIEW, result[7].stage)
        assertEquals(SdlcStage.DONE, result[8].stage)

        result.forEach { stageTime ->
            assertEquals(0L, stageTime.durationMillis)
            assertEquals(0, stageTime.transitionCount)
            assertFalse(stageTime.isBottleneck)
        }
    }

    @Test
    fun calculateStageDwellTimes_fullNineStageTraversal() {
        val baseTime = Instant.parse("2026-08-27T00:00:00Z").toEpochMilli()
        val hour = 3600 * 1000L

        val transitions = listOf(
            StageTransition("issue#1", "status:definition", Instant.ofEpochMilli(baseTime + 1 * hour).toString()),
            StageTransition("issue#1", "status:ready-for-architect", Instant.ofEpochMilli(baseTime + 3 * hour).toString()), // 2h in DEFINITION
            StageTransition("issue#1", "status:needs-po-input", Instant.ofEpochMilli(baseTime + 6 * hour).toString()), // 3h in READY_FOR_ARCHITECT
            StageTransition("issue#1", "status:review", Instant.ofEpochMilli(baseTime + 7 * hour).toString()), // 1h in NEEDS_PO_INPUT
            StageTransition("issue#1", "status:needs-revision", Instant.ofEpochMilli(baseTime + 11 * hour).toString()), // 4h in REVIEW
            StageTransition("issue#1", "status:awaiting-approval", Instant.ofEpochMilli(baseTime + 14 * hour).toString()), // 3h in ARCHITECT_REWORK
            StageTransition("issue#1", "status:in-progress", Instant.ofEpochMilli(baseTime + 16 * hour).toString()), // 2h in AWAITING_APPROVAL
            StageTransition("issue#1", "review:changes-requested", Instant.ofEpochMilli(baseTime + 22 * hour).toString()), // 6h in IN_PROGRESS
            StageTransition("issue#1", "status:done", Instant.ofEpochMilli(baseTime + 26 * hour).toString(), eventType = "closed") // 4h in PR_REVIEW
        )

        val now = baseTime + 30 * hour
        val result = calculateStageDwellTimes(transitions, TimeWindow.THREE_DAYS, nowMillis = now)

        assertEquals(9, result.size)
        assertEquals(2 * hour, result[0].durationMillis) // DEFINITION
        assertEquals(3 * hour, result[1].durationMillis) // READY_FOR_ARCHITECT
        assertEquals(1 * hour, result[2].durationMillis) // NEEDS_PO_INPUT
        assertEquals(4 * hour, result[3].durationMillis) // REVIEW
        assertEquals(3 * hour, result[4].durationMillis) // ARCHITECT_REWORK
        assertEquals(2 * hour, result[5].durationMillis) // AWAITING_APPROVAL
        assertEquals(6 * hour, result[6].durationMillis) // IN_PROGRESS
        assertEquals(4 * hour, result[7].durationMillis) // PR_REVIEW
        assertEquals(0L, result[8].durationMillis) // DONE (closed terminal state)

        // IN_PROGRESS is the bottleneck (6 hours)
        assertTrue(result[6].isBottleneck)
        assertFalse(result[0].isBottleneck)
        assertFalse(result[7].isBottleneck)

        result.forEach { stageTime ->
            assertEquals(1, stageTime.transitionCount)
        }
    }

    @Test
    fun calculateStageDwellTimes_partialStillInProgressTraversal() {
        val baseTime = Instant.parse("2026-08-27T00:00:00Z").toEpochMilli()
        val hour = 3600 * 1000L

        val transitions = listOf(
            StageTransition("issue#2", "status:definition", Instant.ofEpochMilli(baseTime + 2 * hour).toString()),
            StageTransition("issue#2", "status:ready-for-architect", Instant.ofEpochMilli(baseTime + 5 * hour).toString()), // 3h in DEFINITION
            StageTransition("issue#2", "status:in-progress", Instant.ofEpochMilli(baseTime + 10 * hour).toString()) // 5h in READY_FOR_ARCHITECT
        )

        val now = baseTime + 18 * hour // Current time: 8 hours after entering IN_PROGRESS
        val result = calculateStageDwellTimes(transitions, TimeWindow.THREE_DAYS, nowMillis = now)

        assertEquals(9, result.size)
        assertEquals(3 * hour, result[0].durationMillis) // DEFINITION: 3h
        assertEquals(5 * hour, result[1].durationMillis) // READY_FOR_ARCHITECT: 5h
        assertEquals(0L, result[2].durationMillis) // NEEDS_PO_INPUT: 0
        assertEquals(0L, result[3].durationMillis) // REVIEW: 0
        assertEquals(0L, result[4].durationMillis) // ARCHITECT_REWORK: 0
        assertEquals(0L, result[5].durationMillis) // AWAITING_APPROVAL: 0
        assertEquals(8 * hour, result[6].durationMillis) // IN_PROGRESS: now - 10h = 8h
        assertEquals(0L, result[7].durationMillis) // PR_REVIEW: 0
        assertEquals(0L, result[8].durationMillis) // DONE: 0

        // Bottleneck is IN_PROGRESS (8h)
        assertTrue(result[6].isBottleneck)
        assertFalse(result[1].isBottleneck)
    }

    @Test
    fun calculateStageDwellTimes_handlesUnorderedInputTransitions() {
        val baseTime = Instant.parse("2026-08-27T00:00:00Z").toEpochMilli()
        val hour = 3600 * 1000L

        // Transitions in shuffled order
        val transitions = listOf(
            StageTransition("issue#3", "status:in-progress", Instant.ofEpochMilli(baseTime + 10 * hour).toString()),
            StageTransition("issue#3", "status:definition", Instant.ofEpochMilli(baseTime + 2 * hour).toString()),
            StageTransition("issue#3", "status:ready-for-architect", Instant.ofEpochMilli(baseTime + 5 * hour).toString())
        )

        val now = baseTime + 12 * hour
        val result = calculateStageDwellTimes(transitions, TimeWindow.THREE_DAYS, nowMillis = now)

        assertEquals(3 * hour, result[0].durationMillis) // DEFINITION: 5h - 2h = 3h
        assertEquals(5 * hour, result[1].durationMillis) // READY_FOR_ARCHITECT: 10h - 5h = 5h
        assertEquals(2 * hour, result[6].durationMillis) // IN_PROGRESS: 12h - 10h = 2h
    }

    @Test
    fun calculateStageDwellTimes_excludesTransitionsOutsideTimeWindow() {
        val baseTime = Instant.parse("2026-08-27T00:00:00Z").toEpochMilli()
        val day = 24 * 3600 * 1000L

        val now = baseTime + 10 * day

        val transitions = listOf(
            // 7 days ago (outside 3D window, inside 14D window)
            StageTransition("issue#4", "status:definition", Instant.ofEpochMilli(now - 7 * day).toString()),
            // 5 days ago (outside 3D window, inside 14D window)
            StageTransition("issue#4", "status:ready-for-architect", Instant.ofEpochMilli(now - 5 * day).toString()),
            // 2 days ago (inside 3D window)
            StageTransition("issue#4", "status:in-progress", Instant.ofEpochMilli(now - 2 * day).toString()),
            // Future event (beyond now) - should be excluded
            StageTransition("issue#4", "status:done", Instant.ofEpochMilli(now + 1 * day).toString())
        )

        // For 3 Days window: only the transition 2 days ago is included
        val result3d = calculateStageDwellTimes(transitions, TimeWindow.THREE_DAYS, nowMillis = now)
        assertEquals(0L, result3d[0].durationMillis) // DEFINITION excluded
        assertEquals(0L, result3d[1].durationMillis) // READY_FOR_ARCHITECT excluded
        assertEquals(2 * day, result3d[6].durationMillis) // IN_PROGRESS: now - 2 days = 2 days
        assertEquals(0L, result3d[8].durationMillis) // Future DONE excluded

        // For 14 Days window: all 3 valid transitions within window are included
        val result14d = calculateStageDwellTimes(transitions, TimeWindow.TWO_WEEKS, nowMillis = now)
        assertEquals(2 * day, result14d[0].durationMillis) // DEFINITION: 7d to 5d ago = 2d
        assertEquals(3 * day, result14d[1].durationMillis) // READY_FOR_ARCHITECT: 5d to 2d ago = 3d
        assertEquals(2 * day, result14d[6].durationMillis) // IN_PROGRESS: 2d ago to now = 2d
        assertEquals(0L, result14d[8].durationMillis) // Future DONE excluded
    }

    @Test
    fun calculateStageDwellTimes_aggregatesMultipleIssues() {
        val baseTime = Instant.parse("2026-08-27T00:00:00Z").toEpochMilli()
        val hour = 3600 * 1000L

        val transitions = listOf(
            // Issue 1
            StageTransition("issue#1", "status:definition", Instant.ofEpochMilli(baseTime + 1 * hour).toString()),
            StageTransition("issue#1", "status:ready-for-architect", Instant.ofEpochMilli(baseTime + 4 * hour).toString()), // 3h
            // Issue 2
            StageTransition("issue#2", "status:definition", Instant.ofEpochMilli(baseTime + 2 * hour).toString()),
            StageTransition("issue#2", "status:ready-for-architect", Instant.ofEpochMilli(baseTime + 7 * hour).toString()) // 5h
        )

        val now = baseTime + 10 * hour
        val result = calculateStageDwellTimes(transitions, TimeWindow.THREE_DAYS, nowMillis = now)

        // DEFINITION should sum 3h (issue 1) + 5h (issue 2) = 8h
        assertEquals(8 * hour, result[0].durationMillis)
        assertEquals(2, result[0].transitionCount)

        // READY_FOR_ARCHITECT: issue 1 (10h - 4h = 6h) + issue 2 (10h - 7h = 3h) = 9h
        assertEquals(9 * hour, result[1].durationMillis)
        assertEquals(2, result[1].transitionCount)
    }

    @Test
    fun calculateStageDwellTimes_handlesReworkLoops() {
        val baseTime = Instant.parse("2026-08-27T00:00:00Z").toEpochMilli()
        val hour = 3600 * 1000L

        val transitions = listOf(
            StageTransition("issue#1", "status:review", Instant.ofEpochMilli(baseTime + 1 * hour).toString()),
            StageTransition("issue#1", "status:needs-revision", Instant.ofEpochMilli(baseTime + 3 * hour).toString()), // 2h in REVIEW
            StageTransition("issue#1", "status:review", Instant.ofEpochMilli(baseTime + 7 * hour).toString()), // 4h in ARCHITECT_REWORK
            StageTransition("issue#1", "status:ready", Instant.ofEpochMilli(baseTime + 10 * hour).toString()) // 3h in REVIEW (2nd pass)
        )

        val now = baseTime + 12 * hour
        val result = calculateStageDwellTimes(transitions, TimeWindow.THREE_DAYS, nowMillis = now)

        // REVIEW: 2h (1st pass) + 3h (2nd pass) = 5h
        assertEquals(5 * hour, result[3].durationMillis)
        assertEquals(2, result[3].transitionCount)

        // ARCHITECT_REWORK: 4h
        assertEquals(4 * hour, result[4].durationMillis)
        assertEquals(1, result[4].transitionCount)

        // AWAITING_APPROVAL: 12h - 10h = 2h
        assertEquals(2 * hour, result[5].durationMillis)
    }

    @Test
    fun calculateDwellTimeSummary_computesTotalAndBottleneck() {
        val baseTime = Instant.parse("2026-08-27T00:00:00Z").toEpochMilli()
        val hour = 3600 * 1000L

        val transitions = listOf(
            StageTransition("issue#1", "status:definition", Instant.ofEpochMilli(baseTime + 1 * hour).toString()),
            StageTransition("issue#1", "status:ready-for-architect", Instant.ofEpochMilli(baseTime + 5 * hour).toString()), // 4h
            StageTransition("issue#1", "status:done", Instant.ofEpochMilli(baseTime + 7 * hour).toString(), eventType = "closed") // 2h in READY_FOR_ARCHITECT
        )

        val now = baseTime + 10 * hour
        val summary = calculateDwellTimeSummary(transitions, TimeWindow.THREE_DAYS, nowMillis = now)

        assertEquals(9, summary.stageDwellTimes.size)
        assertEquals(6 * hour, summary.totalDwellTimeMillis)
        assertEquals(SdlcStage.DEFINITION, summary.bottleneckStage)
        assertEquals(TimeWindow.THREE_DAYS, summary.timeWindow)
    }

    @Test
    fun formatDuration_formatsCorrectly() {
        assertEquals("0m", formatDuration(0L))
        assertEquals("30m", formatDuration(30 * 60 * 1000L))
        assertEquals("2h", formatDuration(2 * 3600 * 1000L))
        assertEquals("2h 30m", formatDuration(2 * 3600 * 1000L + 30 * 60 * 1000L))
        assertEquals("3d", formatDuration(3 * 24 * 3600 * 1000L))
        assertEquals("3d 5h", formatDuration(3 * 24 * 3600 * 1000L + 5 * 3600 * 1000L))
    }

    @Test
    fun parseTimestampToMillis_handlesIsoAndMillisStrings() {
        assertEquals(1787834000000L, parseTimestampToMillis("1787834000000"))
        assertEquals(1756281600000L, parseTimestampToMillis("2025-08-27T08:00:00Z"))
        assertEquals(0L, parseTimestampToMillis("invalid-timestamp"))
    }

    @Test
    fun stageTransition_instantiatesWithRequiredIssueIdAndDefaults() {
        val transition = StageTransition(
            issueId = "issue-123",
            labelName = "status:in-progress",
            timestamp = "2026-08-29T12:00:00Z"
        )
        assertEquals("issue-123", transition.issueId)
        assertEquals("status:in-progress", transition.labelName)
        assertEquals("2026-08-29T12:00:00Z", transition.timestamp)
        assertEquals("labeled", transition.eventType)
        assertTrue(transition.timestampMillis > 0L)
    }
}

