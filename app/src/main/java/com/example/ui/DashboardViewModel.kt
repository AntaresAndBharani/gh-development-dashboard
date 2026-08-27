package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.StatusTransitionEntity
import com.example.domain.DwellTimeSummary
import com.example.domain.ProjectHealth
import com.example.domain.PullRequest
import com.example.domain.SdlcStage
import com.example.domain.StageDwellTime
import com.example.domain.StageTransition
import com.example.domain.Subtask
import com.example.domain.TimeWindow
import com.example.domain.UserStory
import com.example.domain.calculateDwellTimeSummary
import com.example.domain.calculateStageDwellTimes
import com.example.domain.parseSubtasks
import com.example.repository.ProjectRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

typealias TimeWindow = com.example.domain.TimeWindow

enum class SortOption {
    NAME_ASC, NAME_DESC, STORIES_DESC, PRS_DESC, SUBTASKS_DESC
}

data class AnalyticsUiState(
    val selectedTimeWindow: TimeWindow = TimeWindow.THREE_DAYS,
    val selectedScope: String? = null,
    val stageDwellTimes: List<StageDwellTime> = emptyList(),
    val totalDwellTimeMillis: Long = 0L,
    val bottleneckStage: SdlcStage? = null,
    val summary: DwellTimeSummary? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val isGlobal: Boolean get() = selectedScope.isNullOrBlank() || selectedScope.equals("global", ignoreCase = true)
}

class DashboardViewModel(private val repository: ProjectRepository) : ViewModel() {

    private val _projectsHealth = MutableStateFlow<List<ProjectHealth>>(emptyList())
    val projectsHealth: StateFlow<List<ProjectHealth>> = _projectsHealth.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.NAME_ASC)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _isAddingProject = MutableStateFlow(false)
    val isAddingProject: StateFlow<Boolean> = _isAddingProject.asStateFlow()

    private val _addProjectError = MutableStateFlow<String?>(null)
    val addProjectError: StateFlow<String?> = _addProjectError.asStateFlow()

    private val _selectedTimeWindow = MutableStateFlow(TimeWindow.THREE_DAYS)
    val selectedTimeWindow: StateFlow<TimeWindow> = _selectedTimeWindow.asStateFlow()

    private val _selectedScope = MutableStateFlow<String?>(null)
    val selectedScope: StateFlow<String?> = _selectedScope.asStateFlow()

    private val _analyticsUiState = MutableStateFlow(
        AnalyticsUiState(
            selectedTimeWindow = TimeWindow.THREE_DAYS,
            selectedScope = null,
            stageDwellTimes = calculateStageDwellTimes(emptyList(), TimeWindow.THREE_DAYS),
            summary = calculateDwellTimeSummary(emptyList(), TimeWindow.THREE_DAYS)
        )
    )
    val analyticsUiState: StateFlow<AnalyticsUiState> = _analyticsUiState.asStateFlow()

    private var cachedTransitions: List<StatusTransitionEntity> = emptyList()

    init {
        viewModelScope.launch {
            repository.allProjects.collectLatest { entities ->
                val currentHealthMap = _projectsHealth.value.associateBy { it.id }
                
                val updatedHealthList = entities.map { entity ->
                    currentHealthMap[entity.id] ?: ProjectHealth(
                        id = entity.id,
                        owner = entity.owner,
                        repo = entity.repo,
                        userStories = emptyList(),
                        pullRequests = emptyList(),
                        isLoading = true
                    )
                }
                
                _projectsHealth.value = sortProjects(updatedHealthList, _sortOption.value)
                
                entities.forEach { entity ->
                    fetchProjectData(entity.owner, entity.repo)
                }
            }
        }

        viewModelScope.launch {
            repository.allTransitions?.collectLatest { transitions ->
                cachedTransitions = transitions
                recomputeAnalytics()
            }
        }
    }

    private fun fetchProjectData(owner: String, repo: String) {
        val id = "$owner/$repo"
        viewModelScope.launch {
            try {
                updateProjectState(id) { it.copy(isLoading = true, error = null) }
                
                val issues = repository.getProjectIssues(owner, repo)
                val prs = repository.getProjectPullRequests(owner, repo)
                
                val pullRequests = prs.map { pr ->
                    PullRequest(
                        number = pr.number,
                        title = pr.title,
                        state = pr.state,
                        labels = pr.labels,
                        body = pr.body
                    )
                }

                val userStories = coroutineScope {
                    issues.map { issue ->
                        async {
                            try {
                                val fetched = repository.fetchAndCacheIssueTransitions(owner, repo, issue.number)
                                if (repository.allTransitions == null && fetched.isNotEmpty()) {
                                    synchronized(this@DashboardViewModel) {
                                        val existing = cachedTransitions.filterNot { it.issueId == "$owner/$repo#${issue.number}" }
                                        cachedTransitions = existing + fetched
                                        recomputeAnalytics()
                                    }
                                }
                            } catch (e: Exception) {
                                // Ignore transition fetch errors
                            }

                            val remoteSubIssues = try {
                                repository.getSubIssues(owner, repo, issue.number)
                            } catch (e: Exception) {
                                emptyList()
                            }

                            remoteSubIssues.forEach { sub ->
                                try {
                                    val fetchedSub = repository.fetchAndCacheIssueTransitions(owner, repo, sub.number)
                                    if (repository.allTransitions == null && fetchedSub.isNotEmpty()) {
                                        synchronized(this@DashboardViewModel) {
                                            val existing = cachedTransitions.filterNot { it.issueId == "$owner/$repo#${sub.number}" }
                                            cachedTransitions = existing + fetchedSub
                                            recomputeAnalytics()
                                        }
                                    }
                                } catch (e: Exception) {
                                    // Ignore
                                }
                            }

                            val parsedSubtasks = parseSubtasks(issue.body)
                            val combinedSubtasks = mutableListOf<Subtask>()

                            remoteSubIssues.forEach { sub ->
                                val matchingPr = pullRequests.find { pr ->
                                    pr.number == sub.number ||
                                    pr.title.contains("#${sub.number}") ||
                                    (pr.body?.contains("#${sub.number}") == true) ||
                                    pr.title.equals(sub.title, ignoreCase = true)
                                }
                                combinedSubtasks.add(
                                    Subtask(
                                        number = sub.number,
                                        title = sub.title,
                                        state = sub.state,
                                        isCompleted = sub.state.lowercase() == "closed",
                                        labels = sub.labels ?: emptyList(),
                                        body = sub.body,
                                        linkedNumber = matchingPr?.number ?: sub.number
                                    )
                                )
                            }

                            parsedSubtasks.forEach { parsed ->
                                val alreadyExists = parsed.number != null && combinedSubtasks.any { it.number == parsed.number }
                                if (!alreadyExists) {
                                    val matchingPr = pullRequests.find { pr ->
                                        (parsed.number != null && (pr.number == parsed.number || pr.title.contains("#${parsed.number}") || (pr.body?.contains("#${parsed.number}") == true))) ||
                                        pr.title.contains(parsed.title, ignoreCase = true)
                                    }
                                    combinedSubtasks.add(
                                        parsed.copy(
                                            linkedNumber = matchingPr?.number ?: parsed.linkedNumber
                                        )
                                    )
                                }
                            }

                            UserStory(
                                number = issue.number,
                                title = issue.title,
                                state = issue.state,
                                labels = issue.labels,
                                body = issue.body,
                                subtasks = combinedSubtasks
                            )
                        }
                    }.awaitAll()
                }
                
                updateProjectState(id) {
                    it.copy(
                        userStories = userStories,
                        pullRequests = pullRequests,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                updateProjectState(id) {
                    it.copy(isLoading = false, error = e.localizedMessage ?: "Unknown error")
                }
            }
        }
    }

    private fun updateProjectState(id: String, update: (ProjectHealth) -> ProjectHealth) {
        _projectsHealth.update { list ->
            val updated = list.map { if (it.id == id) update(it) else it }
            sortProjects(updated, _sortOption.value)
        }
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
        _projectsHealth.update { sortProjects(it, option) }
    }

    fun setTimeWindow(window: TimeWindow) {
        _selectedTimeWindow.value = window
        recomputeAnalytics()
    }

    fun setScopeFilter(scope: String?) {
        _selectedScope.value = scope
        recomputeAnalytics()
    }

    private fun recomputeAnalytics() {
        val currentWindow = _selectedTimeWindow.value
        val currentScope = _selectedScope.value

        val filteredEntities = if (currentScope.isNullOrBlank() || currentScope.equals("global", ignoreCase = true)) {
            cachedTransitions
        } else {
            cachedTransitions.filter { entity ->
                val scopeKey = "${entity.owner}/${entity.repo}"
                scopeKey.equals(currentScope, ignoreCase = true) ||
                entity.repo.equals(currentScope, ignoreCase = true)
            }
        }

        val stageTransitions = filteredEntities.map { entity ->
            StageTransition(
                issueId = entity.issueId,
                labelName = entity.labelName,
                timestamp = entity.timestamp,
                eventType = entity.eventType
            )
        }

        val summary = calculateDwellTimeSummary(stageTransitions, currentWindow)

        _analyticsUiState.value = AnalyticsUiState(
            selectedTimeWindow = currentWindow,
            selectedScope = currentScope,
            stageDwellTimes = summary.stageDwellTimes,
            totalDwellTimeMillis = summary.totalDwellTimeMillis,
            bottleneckStage = summary.bottleneckStage,
            summary = summary,
            isLoading = false,
            error = null
        )
    }

    private fun sortProjects(list: List<ProjectHealth>, option: SortOption): List<ProjectHealth> {
        return when (option) {
            SortOption.NAME_ASC -> list.sortedBy { it.repo.lowercase() }
            SortOption.NAME_DESC -> list.sortedByDescending { it.repo.lowercase() }
            SortOption.STORIES_DESC -> list.sortedByDescending { it.openStoriesCount }
            SortOption.PRS_DESC -> list.sortedByDescending { it.openPRsCount }
            SortOption.SUBTASKS_DESC -> list.sortedByDescending { it.totalSubtasks }
        }
    }

    fun addProject(ownerRepoInput: String) {
        val parts = ownerRepoInput.trim().split("/")
        if (parts.size != 2) {
            _addProjectError.value = "Invalid format. Use owner/repo"
            return
        }
        
        val owner = parts[0].trim()
        val repo = parts[1].trim()
        
        if (owner.isEmpty() || repo.isEmpty()) {
            _addProjectError.value = "Owner and repo cannot be empty"
            return
        }

        if (_projectsHealth.value.any { it.id == "$owner/$repo" }) {
            _addProjectError.value = "Project already added"
            return
        }

        viewModelScope.launch {
            _isAddingProject.value = true
            _addProjectError.value = null
            try {
                repository.addProject(owner, repo)
            } catch (e: Exception) {
                _addProjectError.value = "Failed to add repo: ${e.message}"
            } finally {
                _isAddingProject.value = false
            }
        }
    }

    fun removeProject(id: String) {
        viewModelScope.launch {
            repository.deleteProject(id)
        }
    }

    fun refreshAll() {
        val currentList = _projectsHealth.value
        currentList.forEach {
            fetchProjectData(it.owner, it.repo)
        }
    }

    fun clearError() {
        _addProjectError.value = null
    }
}

class DashboardViewModelFactory(
    private val repository: ProjectRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
