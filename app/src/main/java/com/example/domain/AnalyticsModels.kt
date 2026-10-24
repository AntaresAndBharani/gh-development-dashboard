package com.example.domain

import java.time.Instant
import java.time.format.DateTimeParseException

/**
 * Supported time window filters for dwell-time analytics.
 */
enum class TimeWindow(val durationMillis: Long, val displayName: String) {
    THREE_DAYS(3L * 24 * 60 * 60 * 1000, "3 Days"),
    TWO_WEEKS(14L * 24 * 60 * 60 * 1000, "2 Weeks"),
    ONE_MONTH(30L * 24 * 60 * 60 * 1000, "1 Month");

    val durationDays: Int get() = (durationMillis / (24 * 60 * 60 * 1000)).toInt()
}

/**
 * The 9 chronological SDLC workflow phases of the Graph Engineering lifecycle.
 */
enum class SdlcStage(
    val order: Int,
    val displayName: String,
    val primaryLabel: String,
    val alternateLabels: List<String> = emptyList()
) {
    DEFINITION(
        order = 1,
        displayName = "Drafting",
        primaryLabel = "status:definition",
        alternateLabels = listOf("definition", "drafting")
    ),
    READY_FOR_ARCHITECT(
        order = 2,
        displayName = "Architect Decomposition",
        primaryLabel = "status:ready-for-architect",
        alternateLabels = listOf("ready-for-architect")
    ),
    NEEDS_PO_INPUT(
        order = 3,
        displayName = "PO Clarification Escalation",
        primaryLabel = "status:needs-po-input",
        alternateLabels = listOf("needs-po-input")
    ),
    REVIEW(
        order = 4,
        displayName = "Three Amigos Readiness Gate",
        primaryLabel = "status:review",
        alternateLabels = listOf("review", "status:in-review", "in-review", "status:pending-review")
    ),
    ARCHITECT_REWORK(
        order = 5,
        displayName = "Architect Rework",
        primaryLabel = "status:needs-revision",
        alternateLabels = listOf("needs-revision", "status:needs-clarification", "needs-clarification")
    ),
    AWAITING_APPROVAL(
        order = 6,
        displayName = "Cleared for Pickup",
        primaryLabel = "status:awaiting-approval",
        alternateLabels = listOf("awaiting-approval", "status:ready", "ready")
    ),
    IN_PROGRESS(
        order = 7,
        displayName = "Dev & Test Active Implementation",
        primaryLabel = "status:in-progress",
        alternateLabels = listOf("in-progress", "status:in-development", "in-development")
    ),
    PR_REVIEW(
        order = 8,
        displayName = "PR Review Loop",
        primaryLabel = "review:changes-requested",
        alternateLabels = listOf("changes-requested", "review:approved", "approved", "pr-review", "review:review-requested")
    ),
    DONE(
        order = 9,
        displayName = "Merged & Complete",
        primaryLabel = "status:done",
        alternateLabels = listOf("done", "closed", "status:closed")
    );

    companion object {
        fun fromLabel(label: String): SdlcStage? {
            val clean = label.trim().lowercase()
            return entries.find { stage ->
                stage.primaryLabel.lowercase() == clean ||
                stage.alternateLabels.any { it.lowercase() == clean } ||
                stage.primaryLabel.substringAfter(":").lowercase() == clean
            }
        }
    }
}

/**
 * Domain model representing a status/label transition event on an issue or user story.
 */
data class StageTransition(
    val issueId: String,
    val labelName: String,
    val timestamp: String,
    val eventType: String = "labeled"
) {
    val timestampMillis: Long get() = parseTimestampToMillis(timestamp)
}

/**
 * Dwell-time metrics calculated for an individual SDLC stage.
 */
data class StageDwellTime(
    val stage: SdlcStage,
    val durationMillis: Long = 0L,
    val transitionCount: Int = 0,
    val isBottleneck: Boolean = false
) {
    val durationHours: Double get() = durationMillis / (1000.0 * 60 * 60)
    val durationDays: Double get() = durationMillis / (1000.0 * 60 * 60 * 24)
    val formattedDuration: String get() = formatDuration(durationMillis)
}

/**
 * Aggregated summary of dwell times across all 9 stages for a given time window.
 */
data class DwellTimeSummary(
    val stageDwellTimes: List<StageDwellTime>,
    val totalDwellTimeMillis: Long,
    val bottleneckStage: SdlcStage?,
    val timeWindow: TimeWindow
)

/**
 * Helper to parse an ISO-8601 string or epoch string into epoch milliseconds.
 */
fun parseTimestampToMillis(timestamp: String): Long {
    val trimmed = timestamp.trim()
    trimmed.toLongOrNull()?.let { return it }
    return try {
        Instant.parse(trimmed).toEpochMilli()
    } catch (e: DateTimeParseException) {
        0L
    }
}

/**
 * Formats a duration in milliseconds to a human-readable string (e.g., "3d 4h", "2h 30m", "45m").
 */
fun formatDuration(durationMillis: Long): String {
    if (durationMillis <= 0L) return "0m"
    val totalMinutes = durationMillis / (1000 * 60)
    val totalHours = totalMinutes / 60
    val days = totalHours / 24
    val hours = totalHours % 24
    val minutes = totalMinutes % 60

    return when {
        days > 0 && hours > 0 -> "${days}d ${hours}h"
        days > 0 -> "${days}d"
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        minutes > 0 -> "${minutes}m"
        else -> "<1m"
    }
}

/**
 * Computes per-stage dwell-time durations across cached transitions within a selected time window.
 *
 * Guaranteed to return results ordered strictly by the 9-phase chronological SDLC lifecycle:
 * 1. DEFINITION (Drafting)
 * 2. READY_FOR_ARCHITECT (Architect Decomposition)
 * 3. NEEDS_PO_INPUT (PO Clarification Escalation)
 * 4. REVIEW (Three Amigos Readiness Gate)
 * 5. ARCHITECT_REWORK (Architect Rework)
 * 6. AWAITING_APPROVAL (Cleared for Pickup)
 * 7. IN_PROGRESS (Dev & Test Active Implementation)
 * 8. PR_REVIEW (PR Review Loop)
 * 9. DONE (Merged & Complete)
 *
 * @param transitions Raw list of label transition events (order independent).
 * @param timeWindow The selected time filter (3D, 14D, 30D).
 * @param nowMillis The current timestamp reference point in epoch milliseconds (defaults to system time).
 * @return List of 9 [StageDwellTime] objects ordered strictly from stage 1 to stage 9.
 */
fun calculateStageDwellTimes(
    transitions: List<StageTransition>,
    timeWindow: TimeWindow,
    nowMillis: Long = System.currentTimeMillis()
): List<StageDwellTime> {
    val windowStartMillis = nowMillis - timeWindow.durationMillis

    // 1. Filter out transitions that fall outside the selected time window [windowStartMillis, nowMillis]
    val validTransitions = transitions.mapNotNull { transition ->
        val tMillis = transition.timestampMillis
        if (tMillis in windowStartMillis..nowMillis) {
            val stage = SdlcStage.fromLabel(transition.labelName)
            if (stage != null) {
                ParsedTransition(
                    issueId = transition.issueId,
                    stage = stage,
                    timestampMillis = tMillis,
                    eventType = transition.eventType
                )
            } else null
        } else null
    }

    // Accumulators per stage
    val durationMap = mutableMapOf<SdlcStage, Long>()
    val countMap = mutableMapOf<SdlcStage, Int>()

    SdlcStage.entries.forEach { stage ->
        durationMap[stage] = 0L
        countMap[stage] = 0
    }

    // 2. Group transitions by issueId to calculate durations per issue lifecycle
    val groupedByIssue = validTransitions.groupBy { it.issueId }

    for ((_, issueTransitions) in groupedByIssue) {
        val sorted = issueTransitions.sortedBy { it.timestampMillis }
        if (sorted.isEmpty()) continue

        for (i in sorted.indices) {
            val current = sorted[i]
            val stage = current.stage
            countMap[stage] = (countMap[stage] ?: 0) + 1

            val duration = if (i + 1 < sorted.size) {
                val next = sorted[i + 1]
                (next.timestampMillis - current.timestampMillis).coerceAtLeast(0L)
            } else {
                // Last transition for this issue
                if (stage == SdlcStage.DONE || current.eventType.equals("closed", ignoreCase = true)) {
                    // Terminal state reached: 0 active dwell time
                    0L
                } else {
                    // Still in progress up to `nowMillis`
                    (nowMillis - current.timestampMillis).coerceAtLeast(0L)
                }
            }

            durationMap[stage] = (durationMap[stage] ?: 0L) + duration
        }
    }

    // 3. Find bottleneck stage (stage with maximum duration > 0)
    val maxDuration = durationMap.values.maxOrNull() ?: 0L
    val bottleneckStage = if (maxDuration > 0L) {
        durationMap.filter { it.value == maxDuration }.keys.firstOrNull()
    } else null

    // 4. Return strictly ordered 9-stage list (order 1..9)
    return SdlcStage.entries.sortedBy { it.order }.map { stage ->
        val duration = durationMap[stage] ?: 0L
        val count = countMap[stage] ?: 0
        StageDwellTime(
            stage = stage,
            durationMillis = duration,
            transitionCount = count,
            isBottleneck = stage == bottleneckStage && duration > 0L
        )
    }
}

/**
 * Computes the full dwell-time summary including total dwell time and bottleneck stage.
 */
fun calculateDwellTimeSummary(
    transitions: List<StageTransition>,
    timeWindow: TimeWindow,
    nowMillis: Long = System.currentTimeMillis()
): DwellTimeSummary {
    val stageTimes = calculateStageDwellTimes(transitions, timeWindow, nowMillis)
    val totalMillis = stageTimes.sumOf { it.durationMillis }
    val bottleneck = stageTimes.find { it.isBottleneck }?.stage

    return DwellTimeSummary(
        stageDwellTimes = stageTimes,
        totalDwellTimeMillis = totalMillis,
        bottleneckStage = bottleneck,
        timeWindow = timeWindow
    )
}

private data class ParsedTransition(
    val issueId: String,
    val stage: SdlcStage,
    val timestampMillis: Long,
    val eventType: String
)
