package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.ArchitectureComplianceSummary
import com.example.domain.ComplianceState
import com.example.domain.RepoComplianceStatus

/**
 * Main expandable dashboard section displaying cross-repository Architecture & Way of Working
 * compliance (starting with Claude Code Review).
 */
@Composable
fun ArchitectureComplianceSection(
    summary: ArchitectureComplianceSummary,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("architecture_compliance_section"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Architecture,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "ARCHITECTURE & WAY OF WORKING COMPLIANCE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    letterSpacing = 1.sp
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("architecture_compliance_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                // Header Row (Practice Title + Standard Version Chip + Expand Toggle)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                        .testTag("architecture_compliance_toggle"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = Color(0xFF34A853),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = summary.practice.practiceName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                                modifier = Modifier.testTag("architecture_version_chip")
                            ) {
                                Text(
                                    text = "v${summary.practice.standardVersion}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Synchronized across live repos · Updated ${summary.practice.lastUpdated}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (summary.upToDateCount == summary.totalRepos && summary.totalRepos > 0) {
                                Color(0xFF34A853).copy(alpha = 0.15f)
                            } else {
                                Color(0xFFFBBC04).copy(alpha = 0.15f)
                            },
                            border = BorderStroke(
                                1.dp,
                                if (summary.upToDateCount == summary.totalRepos && summary.totalRepos > 0) {
                                    Color(0xFF34A853).copy(alpha = 0.5f)
                                } else {
                                    Color(0xFFFBBC04).copy(alpha = 0.5f)
                                }
                            ),
                            modifier = Modifier.testTag("architecture_summary_ratio")
                        ) {
                            Text(
                                text = "${summary.upToDateCount}/${summary.totalRepos} Up to Date",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (summary.upToDateCount == summary.totalRepos && summary.totalRepos > 0) {
                                    Color(0xFF34A853)
                                } else {
                                    Color(0xFFFBBC04)
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = { expanded = !expanded },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (expanded) "Collapse Architecture Compliance" else "Expand Architecture Compliance",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth((summary.compliancePercent / 100f).coerceIn(0f, 1f))
                            .background(Color(0xFF34A853), CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3-stat summary row: Implemented, Up to Date, Outdated / Pending
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ComplianceStatBox(
                        modifier = Modifier.weight(1f),
                        label = "Implemented",
                        value = "${summary.implementedCount}/${summary.totalRepos}",
                        valueColor = MaterialTheme.colorScheme.primary
                    )
                    ComplianceStatBox(
                        modifier = Modifier.weight(1f),
                        label = "Up to Date",
                        value = summary.upToDateCount.toString(),
                        valueColor = Color(0xFF34A853)
                    )
                    ComplianceStatBox(
                        modifier = Modifier.weight(1f),
                        label = "Needs Sync",
                        value = (summary.outdatedCount + summary.notImplementedCount).toString(),
                        valueColor = if (summary.outdatedCount + summary.notImplementedCount > 0) {
                            Color(0xFFFBBC04)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                // Expandable per-repository compliance breakdown
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "LIVE REPOSITORY ROLLOUT REGISTRY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            letterSpacing = 0.5.sp
                        )

                        summary.repositories.forEach { repoStatus ->
                            ArchitectureRepoRow(status = repoStatus)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ComplianceStatBox(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.outlineVariant,
            fontSize = 9.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
fun ArchitectureRepoRow(
    status: RepoComplianceStatus,
    modifier: Modifier = Modifier
) {
    val (badgeColor, statusIcon) = when (status.state) {
        ComplianceState.UP_TO_DATE -> Color(0xFF34A853) to Icons.Default.CheckCircle
        ComplianceState.OUTDATED -> Color(0xFFFBBC04) to Icons.Default.WarningAmber
        ComplianceState.NOT_IMPLEMENTED -> Color(0xFFBA1A1A) to Icons.Default.ErrorOutline
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("architecture_repo_row_${status.repo}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = status.state.displayName,
                        tint = badgeColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = status.repo,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = status.techStack,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = badgeColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.5f)),
                        modifier = Modifier.testTag("architecture_repo_state_${status.repo}")
                    ) {
                        Text(
                            text = status.state.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    val versionLabel = if (status.syncedVersion != null) {
                        "v${status.syncedVersion} · ${status.lastUpdated ?: ""}"
                    } else {
                        "Target v${status.targetVersion}"
                    }
                    Text(
                        text = versionLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

/**
 * Compact badge rendered on each repository's `ProjectCard` in Active Repositories.
 */
@Composable
fun ProjectComplianceBadge(
    status: RepoComplianceStatus,
    modifier: Modifier = Modifier
) {
    val badgeColor = when (status.state) {
        ComplianceState.UP_TO_DATE -> Color(0xFF34A853)
        ComplianceState.OUTDATED -> Color(0xFFFBBC04)
        ComplianceState.NOT_IMPLEMENTED -> Color(0xFFBA1A1A)
    }

    val badgeText = when (status.state) {
        ComplianceState.UP_TO_DATE -> "Claude Review: Up to Date · v${status.syncedVersion ?: status.targetVersion}"
        ComplianceState.OUTDATED -> "Claude Review: Outdated (${status.syncedVersion?.let { "v$it" } ?: "unregistered"} → v${status.targetVersion})"
        ComplianceState.NOT_IMPLEMENTED -> "Claude Review: Not Implemented"
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = badgeColor.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.45f)),
        modifier = modifier.testTag("project_compliance_badge_${status.repo}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(badgeColor, CircleShape)
            )
            Text(
                text = badgeText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = badgeColor,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * Detailed architecture compliance block shown inside an expanded `ProjectCard`.
 */
@Composable
fun ProjectComplianceDetailsCard(
    status: RepoComplianceStatus,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .padding(12.dp)
            .testTag("project_compliance_details_${status.repo}"),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "ARCHITECTURE & WAY OF WORKING",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.outlineVariant,
            letterSpacing = 0.5.sp
        )
        Text(
            text = "Stack: ${status.techStack}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Workflows: claude.yml (${if (status.hasClaudeYml) "Present" else "Missing"}) · pr-review.yml (${if (status.hasPrReviewYml) "Present" else "Missing"})",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (status.notes.isNotBlank()) {
            Text(
                text = status.notes,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
