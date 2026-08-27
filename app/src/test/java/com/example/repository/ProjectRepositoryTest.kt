package com.example.repository

import com.example.data.local.ProjectDao
import com.example.data.local.ProjectEntity
import com.example.data.local.StatusTransitionDao
import com.example.data.local.StatusTransitionEntity
import com.example.data.remote.CommentDto
import com.example.data.remote.GitHubService
import com.example.data.remote.IssueDto
import com.example.data.remote.IssueEventDto
import com.example.data.remote.LabelDto
import com.example.data.remote.PullRequestDto
import com.example.data.remote.RepoDto
import com.example.data.remote.SubIssueDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeStatusTransitionDao : StatusTransitionDao {
    private val transitions = mutableListOf<StatusTransitionEntity>()

    override fun getAllTransitions(): Flow<List<StatusTransitionEntity>> {
        return flowOf(transitions.toList())
    }

    override suspend fun getAllTransitionsSync(): List<StatusTransitionEntity> {
        return transitions.toList()
    }

    override fun getTransitionsForRepo(owner: String, repo: String): Flow<List<StatusTransitionEntity>> {
        return flowOf(transitions.filter { it.owner == owner && it.repo == repo })
    }

    override suspend fun getTransitionsForRepoSync(owner: String, repo: String): List<StatusTransitionEntity> {
        return transitions.filter { it.owner == owner && it.repo == repo }
    }

    override fun getTransitionsForIssue(issueId: String): Flow<List<StatusTransitionEntity>> {
        return flowOf(transitions.filter { it.issueId == issueId })
    }

    override suspend fun getTransitionsForIssueSync(issueId: String): List<StatusTransitionEntity> {
        return transitions.filter { it.issueId == issueId }
    }

    override suspend fun insertTransitions(transitions: List<StatusTransitionEntity>) {
        this.transitions.addAll(transitions)
    }

    override suspend fun insertTransition(transition: StatusTransitionEntity) {
        this.transitions.add(transition)
    }

    override suspend fun deleteTransitionsForIssue(issueId: String) {
        this.transitions.removeAll { it.issueId == issueId }
    }

    override suspend fun deleteTransitionsForRepo(owner: String, repo: String) {
        this.transitions.removeAll { it.owner == owner && it.repo == repo }
    }

    override suspend fun clearAll() {
        this.transitions.clear()
    }
}

class FakeProjectDao : ProjectDao {
    private val projects = mutableListOf<ProjectEntity>()

    override fun getAllProjects(): Flow<List<ProjectEntity>> {
        return flowOf(projects.toList())
    }

    override suspend fun insertProject(project: ProjectEntity) {
        projects.removeAll { it.id == project.id }
        projects.add(project)
    }

    override suspend fun deleteProjectById(id: String) {
        projects.removeAll { it.id == id }
    }
}

class FakeGitHubService : GitHubService {
    var issueEventsToReturn: List<IssueEventDto> = emptyList()
    var shouldThrowOnEvents: Boolean = false

    override suspend fun getRepo(owner: String, repo: String): RepoDto {
        return RepoDto(name = repo, fullName = "$owner/$repo", description = "Test", stargazersCount = 0)
    }

    override suspend fun getIssues(
        owner: String,
        repo: String,
        state: String,
        labels: String?,
        perPage: Int
    ): List<IssueDto> = emptyList()

    override suspend fun getPullRequests(
        owner: String,
        repo: String,
        state: String,
        perPage: Int
    ): List<PullRequestDto> = emptyList()

    override suspend fun getIssueComments(
        owner: String,
        repo: String,
        issueNumber: Int
    ): List<CommentDto> = emptyList()

    override suspend fun getSubIssues(
        owner: String,
        repo: String,
        issueNumber: Int,
        perPage: Int
    ): List<SubIssueDto> = emptyList()

    override suspend fun getIssueEvents(
        owner: String,
        repo: String,
        issueNumber: Int,
        perPage: Int
    ): List<IssueEventDto> {
        if (shouldThrowOnEvents) {
            throw RuntimeException("Network error")
        }
        return issueEventsToReturn
    }

    override suspend fun getIssue(owner: String, repo: String, issueNumber: Int): IssueDto {
        throw NotImplementedError()
    }

    override suspend fun getPullRequest(owner: String, repo: String, pullNumber: Int): PullRequestDto {
        throw NotImplementedError()
    }
}

class ProjectRepositoryTest {

    private lateinit var projectDao: FakeProjectDao
    private lateinit var statusTransitionDao: FakeStatusTransitionDao
    private lateinit var gitHubService: FakeGitHubService
    private lateinit var repository: ProjectRepository

    @Before
    fun setUp() {
        projectDao = FakeProjectDao()
        statusTransitionDao = FakeStatusTransitionDao()
        gitHubService = FakeGitHubService()
        repository = ProjectRepository(
            projectDao = projectDao,
            gitHubService = gitHubService,
            statusTransitionDao = statusTransitionDao
        )
    }

    @Test
    fun fetchAndCacheIssueTransitions_persistsTransitionsInDao() = runTest {
        gitHubService.issueEventsToReturn = listOf(
            IssueEventDto(
                event = "labeled",
                label = LabelDto(name = "status:definition", color = "ededed"),
                createdAt = "2026-08-27T08:00:00Z"
            ),
            IssueEventDto(
                event = "commented",
                label = null,
                createdAt = "2026-08-27T09:00:00Z"
            ),
            IssueEventDto(
                event = "labeled",
                label = LabelDto(name = "status:ready-for-architect", color = "fbca04"),
                createdAt = "2026-08-27T10:00:00Z"
            )
        )

        val result = repository.fetchAndCacheIssueTransitions("owner", "repo", 42)

        assertEquals(2, result.size)
        assertEquals("owner/repo#42", result[0].issueId)
        assertEquals("status:definition", result[0].labelName)
        assertEquals("2026-08-27T08:00:00Z", result[0].timestamp)
        assertEquals("owner/repo#42", result[1].issueId)
        assertEquals("status:ready-for-architect", result[1].labelName)
        assertEquals("2026-08-27T10:00:00Z", result[1].timestamp)

        val cachedInDao = statusTransitionDao.getTransitionsForIssueSync("owner/repo#42")
        assertEquals(2, cachedInDao.size)
        assertEquals("status:definition", cachedInDao[0].labelName)
        assertEquals("status:ready-for-architect", cachedInDao[1].labelName)
    }

    @Test
    fun fetchAndCacheIssueTransitions_replacesPreviousTransitionsForIssue() = runTest {
        statusTransitionDao.insertTransition(
            StatusTransitionEntity(
                issueId = "owner/repo#42",
                owner = "owner",
                repo = "repo",
                issueNumber = 42,
                labelName = "status:old-stage",
                timestamp = "2026-08-26T12:00:00Z"
            )
        )

        gitHubService.issueEventsToReturn = listOf(
            IssueEventDto(
                event = "labeled",
                label = LabelDto(name = "status:in-progress", color = "0075ca"),
                createdAt = "2026-08-27T14:00:00Z"
            )
        )

        val result = repository.fetchAndCacheIssueTransitions("owner", "repo", 42)

        assertEquals(1, result.size)
        assertEquals("status:in-progress", result[0].labelName)

        val cachedInDao = statusTransitionDao.getTransitionsForIssueSync("owner/repo#42")
        assertEquals(1, cachedInDao.size)
        assertEquals("status:in-progress", cachedInDao[0].labelName)
    }

    @Test
    fun fetchAndCacheIssueTransitions_handlesErrorGracefully() = runTest {
        gitHubService.shouldThrowOnEvents = true

        val result = repository.fetchAndCacheIssueTransitions("owner", "repo", 42)

        assertTrue(result.isEmpty())
        val cachedInDao = statusTransitionDao.getTransitionsForIssueSync("owner/repo#42")
        assertTrue(cachedInDao.isEmpty())
    }
}