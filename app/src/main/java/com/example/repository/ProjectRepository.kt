package com.example.repository

import com.example.data.local.ProjectDao
import com.example.data.local.ProjectEntity
import com.example.data.remote.GitHubService
import com.example.data.remote.IssueDto
import com.example.data.remote.PullRequestDto
import com.example.data.remote.SubIssueDto
import kotlinx.coroutines.flow.Flow

class ProjectRepository(
    private val projectDao: ProjectDao,
    private val gitHubService: GitHubService
) {
    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()

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
}
