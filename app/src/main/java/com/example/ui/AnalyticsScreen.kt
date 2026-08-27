package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.ProjectHealth
import com.example.domain.SdlcStage
import com.example.domain.StageDwellTime
import com.example.domain.TimeWindow
import com.example.domain.formatDuration

/**
 * Main Analytics section mounted on the Dashboard screen.
 */
@Composable
fun AnalyticsSection(
    uiState: AnalyticsUiState,
    projects: List<ProjectHealth>,
    onTimeWindowSelected: (TimeWindow) -> Unit,
    onScopeSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedDwellCards by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("analytics_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.Timeline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    "WORKFLOW DWELL-TIME ANALYTICS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    letterSpacing = 1.sp
                )
            }
        }

        // Scope Bar (Global vs per-project selector)
        ScopeBar(
            selectedScope = uiState.selectedScope,
            projects = projects,
            onScopeSelected = onScopeSelected
        )

        // Segmented Time-Range Selection & Total Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                // Time Range Segmented Control
                TimeRangeSelector(
                    selectedWindow = uiState.selectedTimeWindow,
                    onWindowSelected = onTimeWindowSelected
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Summary Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "TOTAL DWELL TIME",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatDuration(uiState.totalDwellTimeMillis),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (uiState.bottleneckStage != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                            modifier = Modifier.testTag("bottleneck_summary_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Bottleneck",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Column {
                                    Text(
                                        "BOTTLENECK DETECTED",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 9.sp
                                    )
                                    Text(
                                        uiState.bottleneckStage.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Story Evolution Chart (Compose Canvas)
                StoryEvolutionChart(
                    stageDwellTimes = uiState.stageDwellTimes,
                    bottleneckStage = uiState.bottleneckStage
                )
            }
        }

        // Stage Dwell-Time Cards Header & Expandable Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedDwellCards = !expandedDwellCards },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "STAGE DWELL-TIME BREAKDOWN",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            "9 Chronological SDLC Stages",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = { expandedDwellCards = !expandedDwellCards }) {
                        Icon(
                            imageVector = if (expandedDwellCards) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expandedDwellCards) "Collapse Stages" else "Expand Stages",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                AnimatedVisibility(
                    visible = expandedDwellCards,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.stageDwellTimes.forEach { dwellTime ->
                            StageDwellTimeCard(
                                dwellTime = dwellTime,
                                isBottleneck = dwellTime.isBottleneck || dwellTime.stage == uiState.bottleneckStage
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Scope Bar: Filter between "Global / All Projects" and individual repository scopes.
 */
@Composable
fun ScopeBar(
    selectedScope: String?,
    projects: List<ProjectHealth>,
    onScopeSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isGlobalSelected = selectedScope.isNullOrBlank() || selectedScope.equals("global", ignoreCase = true)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .testTag("scope_bar"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Global / All Projects Scope Chip
        FilterChip(
            selected = isGlobalSelected,
            onClick = { onScopeSelected(null) },
            label = { Text("Global / All Projects", fontWeight = if (isGlobalSelected) FontWeight.Bold else FontWeight.Normal) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("scope_chip_global")
        )

        // Individual Project Scope Chips
        projects.forEach { project ->
            val isSelected = !isGlobalSelected && (
                selectedScope.equals(project.id, ignoreCase = true) ||
                selectedScope.equals(project.repo, ignoreCase = true)
            )
            FilterChip(
                selected = isSelected,
                onClick = { onScopeSelected(project.id) },
                label = { Text(project.repo, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("scope_chip_${project.repo}")
            )
        }
    }
}

/**
 * Segmented Time-Range Selector supporting 3 Days (72h), 2 Weeks (14d), and 1 Month (30d).
 */
@Composable
fun TimeRangeSelector(
    selectedWindow: TimeWindow,
    onWindowSelected: (TimeWindow) -> Unit,
    modifier: Modifier = Modifier
) {
    val windows = listOf(
        TimeWindow.THREE_DAYS to "3D (72h)",
        TimeWindow.TWO_WEEKS to "14D (2W)",
        TimeWindow.ONE_MONTH to "30D (1M)"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(4.dp)
            .testTag("time_range_selector"),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        windows.forEach { (window, label) ->
            val isSelected = selectedWindow == window
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onWindowSelected(window) }
                    .padding(vertical = 8.dp)
                    .testTag("time_window_${window.name}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Story Evolution Chart built with Compose Canvas.
 * Visualizes dwell times across the 9 chronological SDLC stages and flags bottlenecks.
 */
@Composable
fun StoryEvolutionChart(
    stageDwellTimes: List<StageDwellTime>,
    bottleneckStage: SdlcStage?,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val outlineColor = MaterialTheme.colorScheme.outline
    val gridColor = outlineColor.copy(alpha = 0.6f)

    // Stage short labels for 9 chronological stages
    val shortLabels = listOf("DEF", "ARCH", "PO", "REV", "REW", "APR", "DEV", "PR", "DONE")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("story_evolution_chart")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ShowChart,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    "Story Evolution Pipeline",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                "Stage 1 → 9 Flow",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .border(1.dp, outlineColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize().testTag("story_evolution_canvas")) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val bottomPadding = 16f
                val topPadding = 12f
                val chartHeight = canvasHeight - bottomPadding - topPadding

                val stagesCount = if (stageDwellTimes.isNotEmpty()) stageDwellTimes.size else 9
                val slotWidth = canvasWidth / stagesCount
                val barWidth = slotWidth * 0.55f

                // Draw background grid lines (horizontal)
                val gridLines = 3
                for (g in 0..gridLines) {
                    val y = topPadding + (chartHeight / gridLines) * g
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1f
                    )
                }

                val maxDuration = stageDwellTimes.maxOfOrNull { it.durationMillis } ?: 0L
                val barPoints = mutableListOf<Offset>()

                for (index in 0 until stagesCount) {
                    val dwell = stageDwellTimes.getOrNull(index)
                    val duration = dwell?.durationMillis ?: 0L
                    val isBottleneck = (dwell?.isBottleneck == true) || (dwell?.stage == bottleneckStage && duration > 0L)

                    val barHeight = if (maxDuration > 0L) {
                        val ratio = (duration.toFloat() / maxDuration).coerceIn(0f, 1f)
                        (ratio * (chartHeight - 8f)).coerceAtLeast(4f)
                    } else {
                        4f
                    }

                    val x = slotWidth * index + (slotWidth - barWidth) / 2f
                    val y = topPadding + chartHeight - barHeight

                    val barColor = if (isBottleneck) errorColor else primaryColor.copy(alpha = 0.85f)

                    // Draw individual stage bar
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )

                    val centerX = x + barWidth / 2f
                    barPoints.add(Offset(centerX, y))
                }

                // Draw trend/evolution curve connecting stages
                if (barPoints.size > 1) {
                    val path = Path()
                    path.moveTo(barPoints.first().x, barPoints.first().y)
                    for (i in 1 until barPoints.size) {
                        val prev = barPoints[i - 1]
                        val curr = barPoints[i]
                        val midX = (prev.x + curr.x) / 2f
                        path.cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                    }
                    drawPath(
                        path = path,
                        color = primaryColor.copy(alpha = 0.4f),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Draw baseline
                drawLine(
                    color = outlineColor,
                    start = Offset(0f, topPadding + chartHeight),
                    end = Offset(canvasWidth, topPadding + chartHeight),
                    strokeWidth = 2f
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Stage labels row underneath chart
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val stagesCount = if (stageDwellTimes.isNotEmpty()) stageDwellTimes.size else 9
            for (i in 0 until stagesCount) {
                val dwell = stageDwellTimes.getOrNull(i)
                val isBottleneck = (dwell?.isBottleneck == true) || (dwell?.stage == bottleneckStage && (dwell?.durationMillis ?: 0L) > 0L)
                val label = shortLabels.getOrElse(i) { "${i + 1}" }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${i + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isBottleneck) FontWeight.Bold else FontWeight.Medium,
                        color = if (isBottleneck) errorColor else MaterialTheme.colorScheme.primary,
                        fontSize = 9.sp
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isBottleneck) FontWeight.Bold else FontWeight.Normal,
                        color = if (isBottleneck) errorColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 8.sp
                    )
                }
            }
        }
    }
}

/**
 * Individual Stage Dwell-Time Card visually highlighting bottlenecks.
 */
@Composable
fun StageDwellTimeCard(
    dwellTime: StageDwellTime,
    isBottleneck: Boolean,
    modifier: Modifier = Modifier
) {
    val stage = dwellTime.stage
    val errorColor = MaterialTheme.colorScheme.error
    val cardBorder = if (isBottleneck) {
        BorderStroke(1.5.dp, errorColor)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
    }

    val cardBg = if (isBottleneck) {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("stage_card_${stage.name}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = cardBorder
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Stage number badge
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                if (isBottleneck) errorColor else MaterialTheme.colorScheme.primary,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${stage.order}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }

                    Column {
                        Text(
                            text = stage.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isBottleneck) errorColor else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stage.primaryLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            fontSize = 10.sp
                        )
                    }
                }

                // Dwell Duration
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = dwellTime.formattedDuration,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isBottleneck) errorColor else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${dwellTime.transitionCount} trans",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }

            if (isBottleneck) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = errorColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, errorColor.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth().testTag("bottleneck_flag_${stage.name}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = errorColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "BOTTLENECK: Longest dwell-time in current workflow",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = errorColor,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
