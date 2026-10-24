package com.example.repository

import com.example.data.local.ProjectDao
import com.example.data.local.ProjectEntity
import com.example.data.local.StatusTransitionDao
import com.example.data.local.StatusTransitionEntity
import com.example.data.remote.GitHubService
import com.example.data.remote.IssueDto
import com.example.data.remote.PullRequestDto
import com.example.data.remote.SubIssueDto
import com.example.domain.ArchitectureRegistry
import com.example.domain.ArchitectureRegistryDocument
import com.example.domain.RepoComplianceStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import retrofit2.HttpException

class ProjectRepository(
    private val projectDao: ProjectDao,
    private val gitHubService: GitHubService,
    private val statusTransitionDao: StatusTransitionDao? = null,
    private val architectureRegistry: ArchitectureRegistryDocument = ArchitectureRegistry.DEFAULT_DOCUMENT
) {
    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()
    val allTransitions: Flow<List<StatusTransitionEntity>>? = statusTransitionDao?.getAllTransitions()

    fun getArchitectureRegistry(): ArchitectureRegistryDocument = architectureRegistry

    fun getTransitionsForProject(owner: String, repo: String): Flow<List<StatusTransitionEntity>>? {
        return statusTransitionDao?.getTransitionsForRepo(owner, repo)
    }

    fun getTransitionsForIssue(issueId: String): Flow<List<StatusTransitionEntity>>? {
        return statusTransitionDao?.getTransitionsForIssue(issueId)
    }

    /**
     * Seeds the 5 live AntaresAndBharani repositories into local storage if no projects are currently stored.
     */
    suspend fun seedLiveProjectsIfEmpty() {
        val current = try {
            projectDao.getAllProjects().first()
        } catch (e: Exception) {
            emptyList()
        }
        if (current.isEmpty()) {
            architectureRegistry.repositories.forEach { entry ->
                projectDao.insertProject(
                    ProjectEntity(
                        id = entry.repoId,
                        owner = entry.owner,
                        repo = entry.repo
                    )
                )
            }
        }
    }

    /**
     * Checks whether a repository has `.github/workflows/claude.yml` and `.github/workflows/pr-review.yml`
     * via the GitHub API and combines that signal with `architectureRegistry` to compute `RepoComplianceStatus`.
     *
     * - HTTP 404 (or message containing "404") -> file is definitively missing (`false`).
     * - Other network/IO errors -> returns `null` so `ArchitectureRegistry.evaluateRepoCompliance` falls back
     *   to the baseline entry in `architecture-registry.json`.
     */
    suspend fun checkRepoArchitectureCompliance(owner: String, repo: String): RepoComplianceStatus {
        val hasClaudeYml = checkRemoteFileExists(owner, repo, ArchitectureRegistry.CLAUDE_WORKFLOW_PATH)
        val hasPrReviewYml = checkRemoteFileExists(owner, repo, ArchitectureRegistry.PR_REVIEW_WORKFLOW_PATH)

        return ArchitectureRegistry.evaluateRepoCompliance(
            owner = owner,
            repo = repo,
            hasClaudeYml = hasClaudeYml,
            hasPrReviewYml = hasPrReviewYml,
            registry = architectureRegistry
        )
    }

    private suspend fun checkRemoteFileExists(owner: String, repo: String, path: String): Boolean? {
        return try {
            val content = gitHubService.getRepoFileContent(owner, repo, path)
            content.path.isNotBlank()
        } catch (e: HttpException) {
            if (e.code() == 404) {
                false
            } else {
                null
            }
        } catch (e: Exception) {
            val msg = e.message ?: ""
            if (msg.contains("404") || msg.contains("Not Found", ignoreCase = true)) {
                false
            } else {
                null
            }
        }
    }

    suspend fun addProject(owner: String, repo: String) {
        // Just verify if it exists, throws exception if not found
        gitHubService.getRepo(owner, repo)
        projectDao.insertProject(ProjectEntity(id = "${owner}/${repo}", owner = owner, repo = repo))
    }

    suspend fun deleteProject(id: String) {
        projectDao.deleteProjectById(id)
    }

    suspend fun getProjectIssues(owner: String, repo: String): List<IssueDto> {
        val storyIssues = try {
            gitHubService.getIssues(owner = owner, repo = repo, labels = "type:user-story")
        } catch (e: Exception) {
            emptyList()
        }
        if (storyIssues.isNotEmpty()) {
            return storyIssues.filter { it.pullRequest == null }
        }
        val allIssues = try {
            gitHubService.getIssues(owner = owner, repo = repo)
        } catch (e: Exception) {
            emptyList()
        }
        val filtered = allIssues.filter { issue ->
            issue.pullRequest == null && (
                issue.labels.any { it.name.contains("story", ignoreCase = true) || it.name.contains("user-story", ignoreCase = true) }
            )
        }
        return if (filtered.isNotEmpty()) filtered else allIssues.filter { it.pullRequest == null }
    }

    suspend fun getSubIssues(owner: String, repo: String, issueNumber: Int): List<SubIssueDto> {
        return gitHubService.getSubIssues(owner = owner, repo = repo, issueNumber = issueNumber)
    }

    suspend fun getProjectPullRequests(owner: String, repo: String): List<PullRequestDto> {
        return gitHubService.getPullRequests(owner = owner, repo = repo)
    }

    suspend fun fetchAndCacheIssueTransitions(
        owner: String,
        repo: String,
        issueNumber: Int
    ): List<StatusTransitionEntity> {
        val issueId = "$owner/$repo#$issueNumber"
        val events = try {
            gitHubService.getIssueEvents(owner = owner, repo = repo, issueNumber = issueNumber)
        } catch (e: Exception) {
            emptyList()
        }

        val transitions = events.filter { it.label != null }.map { event ->
            StatusTransitionEntity(
                issueId = issueId,
                owner = owner,
                repo = repo,
                issueNumber = issueNumber,
                labelName = event.label!!.name,
                eventType = event.event,
                timestamp = event.createdAt
            )
        }

        statusTransitionDao?.let { dao ->
            dao.deleteTransitionsForIssue(issueId)
            if (transitions.isNotEmpty()) {
                dao.insertTransitions(transitions)
            }
        }

        return transitions
    }

    suspend fun fetchAndCacheStatusTransitions(
        owner: String,
        repo: String,
        issueNumber: Int
    ): List<StatusTransitionEntity> {
        return fetchAndCacheIssueTransitions(owner, repo, issueNumber)
    }
}
