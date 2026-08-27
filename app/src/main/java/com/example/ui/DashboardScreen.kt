package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.remote.RetrofitClient
import kotlinx.coroutines.launch
import com.example.data.remote.LabelDto
import com.example.domain.ProjectHealth
import com.example.domain.PullRequest
import com.example.domain.Subtask
import com.example.domain.UserStory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val projects by viewModel.projectsHealth.collectAsStateWithLifecycle()
    val isAdding by viewModel.isAddingProject.collectAsStateWithLifecycle()
    val error by viewModel.addProjectError.collectAsStateWithLifecycle()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var sortExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Analytics, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        Text("GitScope", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.titleLarge, letterSpacing = (-0.5).sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                actions = {
                    IconButton(onClick = { viewModel.refreshAll() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    Box {
                        IconButton(onClick = { sortExpanded = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = sortExpanded,
                            onDismissRequest = { sortExpanded = false }
                        ) {
                            DropdownMenuItem(text = { Text("Name (A-Z)") }, onClick = { viewModel.setSortOption(SortOption.NAME_ASC); sortExpanded = false })
                            DropdownMenuItem(text = { Text("Name (Z-A)") }, onClick = { viewModel.setSortOption(SortOption.NAME_DESC); sortExpanded = false })
                            DropdownMenuItem(text = { Text("Most User Stories") }, onClick = { viewModel.setSortOption(SortOption.STORIES_DESC); sortExpanded = false })
                            DropdownMenuItem(text = { Text("Most PRs") }, onClick = { viewModel.setSortOption(SortOption.PRS_DESC); sortExpanded = false })
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Project")
            }
        }
    ) { padding ->
        if (projects.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No projects added. Click + to add.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    val totalStories = projects.sumOf { it.openStoriesCount }
                    val totalTasks = projects.sumOf { it.totalSubtasks }
                    val completedTasks = projects.sumOf { it.completedSubtasks }
                    val healthPercent = if (totalTasks > 0) (completedTasks * 100) / totalTasks else 100
                    
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                                Column {
                                    Text("UNIFIED HEALTH", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${healthPercent}%", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                                }
                                Box(
                                    modifier = Modifier.background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text("${projects.size} Projects", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)) {
                                Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(healthPercent / 100f).background(Color.White, CircleShape))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("$totalStories user stories pending across all repos", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
                        }
                    }
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ACTIVE REPOSITORIES", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.outlineVariant, letterSpacing = 1.sp)
                    }
                }
                
                items(projects, key = { it.id }) { project ->
                    ProjectCard(project = project, onDelete = { viewModel.removeProject(project.id) })
                }
            }
        }
    }

    if (showAddDialog) {
        AddProjectDialog(
            isAdding = isAdding,
            onDismiss = { showAddDialog = false },
            onAdd = { ownerRepo -> 
                viewModel.addProject(ownerRepo)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddProjectDialog(isAdding: Boolean, onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var input by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add GitHub Repository") },
        text = {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("owner/repo") },
                placeholder = { Text("e.g. google/accompanist") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onAdd(input) },
                enabled = input.isNotBlank() && !isAdding
            ) {
                if (isAdding) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Add")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ProjectCard(project: ProjectHealth, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(project.id.replace("/", " / "), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Box(modifier = Modifier.size(8.dp).background(if (project.error != null) Color(0xFFBA1A1A) else if (project.isLoading) Color(0xFFFBBC04) else Color(0xFF34A853), CircleShape))
                    }
                }
                
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (project.isLoading) {
                Text("Loading data...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            } else if (project.error != null) {
                Text("Error: ${project.error}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val taskPercent = if (project.totalSubtasks > 0) "${(project.completedSubtasks * 100) / project.totalSubtasks}%" else "0%"
                    
                    StatBox(modifier = Modifier.weight(1f), label = "Stories", value = project.openStoriesCount.toString())
                    StatBox(modifier = Modifier.weight(1f), label = "Tasks", value = taskPercent)
                    StatBox(modifier = Modifier.weight(1f), label = "PRs", value = project.openPRsCount.toString())
                }
            }
            
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (project.userStories.isNotEmpty()) {
                        Column {
                            Text("User Stories", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            project.userStories.forEach { story ->
                                UserStoryItem(story = story, project = project)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    } else if (!project.isLoading) {
                        Text("No open user stories", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun StatBox(modifier: Modifier = Modifier, label: String, value: String) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outlineVariant, fontSize = 10.sp)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun UserStoryItem(story: UserStory, project: ProjectHealth) {
    var expanded by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("#${story.number}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(story.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand Story",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("STATUS:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outlineVariant)
                            val statusColor = if (story.state.lowercase() == "open") Color(0xFF34A853) else Color(0xFFBA1A1A)
                            Box(
                                modifier = Modifier
                                    .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(story.state.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = statusColor, fontSize = 10.sp)
                            }
                        }

                        // Button to view description and comments for the Story
                        TextButton(
                            onClick = { showDetailsDialog = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Notes, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Description & Comments", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (story.labels.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            story.labels.forEach { label ->
                                LabelChip(label)
                            }
                        }
                    }

                    if (story.subtasks.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("SUBTASKS (${story.subtasks.size})", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outlineVariant, letterSpacing = 1.sp)
                            val completed = story.subtasks.count { it.isCompleted }
                            Text("$completed/${story.subtasks.size} done", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        story.subtasks.forEach { task ->
                            SubtaskItem(task = task, project = project)
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    } else {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No subtasks linked to this story.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    if (showDetailsDialog) {
        ItemDetailsBottomSheet(
            title = "#${story.number} ${story.title}",
            itemType = "User Story",
            owner = project.owner,
            repo = project.repo,
            issueNumber = story.number,
            description = story.body,
            onDismiss = { showDetailsDialog = false }
        )
    }
}

@Composable
fun SubtaskItem(task: Subtask, project: ProjectHealth) {
    var expanded by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }

    val linkedPr = project.pullRequests.find { pr ->
        (task.linkedNumber != null && pr.number == task.linkedNumber) ||
        (task.number != null && pr.number == task.number) ||
        (task.number != null && (pr.title.contains("#${task.number}") || (pr.body?.contains("#${task.number}") == true)))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .clickable { expanded = !expanded }
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = if (task.isCompleted) "Completed" else "Pending",
                tint = if (task.isCompleted) Color(0xFF34A853) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (task.number != null) {
                        Text("#${task.number}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Text(task.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                }
                if (task.labels.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        task.labels.forEach { LabelChip(it) }
                    }
                }
            }
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Expand Subtask",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
        
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth()
            ) {
                // Subtask action row with Details Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("PULL REQUESTS & PR LINK", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outlineVariant, letterSpacing = 0.5.sp)
                    
                    TextButton(
                        onClick = { showDetailsDialog = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Notes, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Subtask Details", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // LEVEL 3: Pure Pull Request Level
                if (linkedPr != null) {
                    PullRequestItem(pr = linkedPr, project = project)
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "No linked Pull Request found for this subtask.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showDetailsDialog) {
        val taskNum = task.number ?: task.linkedNumber
        ItemDetailsBottomSheet(
            title = if (task.number != null) "#${task.number} ${task.title}" else task.title,
            itemType = "Subtask Issue",
            owner = project.owner,
            repo = project.repo,
            issueNumber = taskNum,
            description = task.body,
            onDismiss = { showDetailsDialog = false }
        )
    }
}

@Composable
fun PullRequestItem(pr: PullRequest, project: ProjectHealth) {
    var showDetailsDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.MergeType,
                contentDescription = "Pull Request",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("#${pr.number}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.width(6.dp))
            Text(pr.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(6.dp))
            val prStatusColor = when (pr.state.lowercase()) {
                "open" -> Color(0xFF34A853)
                "closed" -> Color(0xFF6750A4)
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            Box(
                modifier = Modifier
                    .background(prStatusColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    .border(1.dp, prStatusColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(pr.state.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = prStatusColor, fontSize = 10.sp)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(
                onClick = { showDetailsDialog = true },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Icon(Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("View PR Description & Comments", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
        }
    }

    if (showDetailsDialog) {
        ItemDetailsBottomSheet(
            title = "PR #${pr.number}: ${pr.title}",
            itemType = "Pull Request",
            owner = project.owner,
            repo = project.repo,
            issueNumber = pr.number,
            description = pr.body,
            onDismiss = { showDetailsDialog = false }
        )
    }
}

@Composable
fun ItemDetailsBottomSheet(
    title: String,
    itemType: String,
    owner: String,
    repo: String,
    issueNumber: Int?,
    description: String?,
    onDismiss: () -> Unit
) {
    val lastCommentState = produceState<Pair<String?, String?>?>(initialValue = null, owner, repo, issueNumber) {
        if (issueNumber != null) {
            try {
                val comments = RetrofitClient.instance.getIssueComments(owner, repo, issueNumber)
                val last = comments.lastOrNull()
                value = if (last != null) Pair(last.user?.login ?: "User", last.body) else Pair(null, null)
            } catch (e: Exception) {
                value = Pair(null, null)
            }
        } else {
            value = Pair(null, null)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(itemType.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text("DESCRIPTION", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outlineVariant, letterSpacing = 0.5.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = if (!description.isNullOrBlank()) description.trim() else "No description provided.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    item {
                        Text("LAST COMMENT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outlineVariant, letterSpacing = 0.5.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        if (lastCommentState.value == null && issueNumber != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                Text("Loading latest discussion comment...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            val commentData = lastCommentState.value
                            if (commentData?.second != null) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "@${commentData.first}:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = commentData.second!!.trim(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Text("No comments on this item.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun LabelChip(label: LabelDto) {
    val parseColor = try {
        Color(android.graphics.Color.parseColor("#${label.color}"))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    // determine if we should use dark or light text on this chip
    // very simplified luminance check
    val luminance = (0.299 * parseColor.red + 0.587 * parseColor.green + 0.114 * parseColor.blue)
    val textColor = if (luminance > 0.5) Color(0xFF001D36) else Color.White
    val bgColor = if (luminance > 0.5) parseColor.copy(alpha = 0.3f) else parseColor

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, parseColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(label.name, style = MaterialTheme.typography.labelSmall, color = textColor)
    }
}
