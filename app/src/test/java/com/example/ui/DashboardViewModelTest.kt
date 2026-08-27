package com.example.ui

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
import com.example.domain.SdlcStage
import com.example.domain.TimeWindow
import com.example.repository.ProjectRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class TestProjectDao : ProjectDao {
    val projectsFlow = MutableStateFlow<List<ProjectEntity>>(emptyList())

    override fun getAllProjects(): Flow<List<ProjectEntity>> = projectsFlow.asStateFlow()

    override suspend fun insertProject(project: ProjectEntity) {
        val current = projectsFlow.value.toMutableList()
        current.removeAll { it.id == project.id }
        current.add(project)
        projectsFlow.value = current
    }

    override suspend fun deleteProjectById(id: String) {
        val current = projectsFlow.value.toMutableList()
        current.removeAll { it.id == id }
        projectsFlow.value = current
    }
}

class TestStatusTransitionDao : StatusTransitionDao {
    val transitionsFlow = MutableStateFlow<List<StatusTransitionEntity>>(emptyList())

    override fun getAllTransitions(): Flow<List<StatusTransitionEntity>> = transitionsFlow.asStateFlow()

    override suspend fun getAllTransitionsSync(): List<StatusTransitionEntity> = transitionsFlow.value

    override fun getTransitionsForRepo(owner: String, repo: String): Flow<List<StatusTransitionEntity>> {
        return flowOf(transitionsFlow.value.filter { it.owner == owner && it.repo == repo })
    }

    override suspend fun getTransitionsForRepoSync(owner: String, repo: String): List<StatusTransitionEntity> {
        return transitionsFlow.value.filter { it.owner == owner && it.repo == repo }
    }

    override fun getTransitionsForIssue(issueId: String): Flow<List<StatusTransitionEntity>> {
        return flowOf(transitionsFlow.value.filter { it.issueId == issueId })
    }

    override suspend fun getTransitionsForIssueSync(issueId: String): List<StatusTransitionEntity> {
        return transitionsFlow.value.filter { it.issueId == issueId }
    }

    override suspend fun insertTransitions(transitions: List<StatusTransitionEntity>) {
        val current = transitionsFlow.value.toMutableList()
        current.addAll(transitions)
        transitionsFlow.value = current
    }

    override suspend fun insertTransition(transition: StatusTransitionEntity) {
        val current = transitionsFlow.value.toMutableList()
        current.add(transition)
        transitionsFlow.value = current
    }

    override suspend fun deleteTransitionsForIssue(issueId: String) {
        val current = transitionsFlow.value.toMutableList()
        current.removeAll { it.issueId == issueId }
        transitionsFlow.value = current
    }

    override suspend fun deleteTransitionsForRepo(owner: String, repo: String) {
        val current = transitionsFlow.value.toMutableList()
        current.removeAll { it.owner == owner && it.repo == repo }
        transitionsFlow.value = current
    }

    override suspend fun clearAll() {
        transitionsFlow.value = emptyList()
    }
}

class TestGitHubService : GitHubService {
    var issuesToReturn: List<IssueDto> = emptyList()
    var prsToReturn: List<PullRequestDto> = emptyList()
    var subIssuesMap: MutableMap<Int, List<SubIssueDto>> = mutableMapOf()
    var issueEventsMap: MutableMap<Int, List<IssueEventDto>> = mutableMapOf()
    var getIssuesCallCount = 0
    var getIssueEventsCallCount = 0

    override suspend fun getRepo(owner: String, repo: String): RepoDto {
        return RepoDto(name = repo, fullName = "$owner/$repo", description = "Test Repo", stargazersCount = 5)
    }

    override suspend fun getIssues(
        owner: String,
        repo: String,
        state: String,
        labels: String?,
        perPage: Int
    ): List<IssueDto> {
        getIssuesCallCount++
        return issuesToReturn
    }

    override suspend fun getPullRequests(
        owner: String,
        repo: String,
        state: String,
        perPage: Int
    ): List<PullRequestDto> {
        return prsToReturn
    }

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
    ): List<SubIssueDto> {
        return subIssuesMap[issueNumber] ?: emptyList()
    }

    override suspend fun getIssueEvents(
        owner: String,
        repo: String,
        issueNumber: Int,
        perPage: Int
    ): List<IssueEventDto> {
        getIssueEventsCallCount++
        return issueEventsMap[issueNumber] ?: emptyList()
    }

    override suspend fun getIssue(owner: String, repo: String, issueNumber: Int): IssueDto {
        throw NotImplementedError()
    }

    override suspend fun getPullRequest(owner: String, repo: String, pullNumber: Int): PullRequestDto {
        throw NotImplementedError()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var projectDao: TestProjectDao
    private lateinit var statusTransitionDao: TestStatusTransitionDao
    private lateinit var gitHubService: TestGitHubService
    private lateinit var repository: ProjectRepository
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        projectDao = TestProjectDao()
        statusTransitionDao = TestStatusTransitionDao()
        gitHubService = TestGitHubService()
        repository = ProjectRepository(
            projectDao = projectDao,
            gitHubService = gitHubService,
            statusTransitionDao = statusTransitionDao
        )
        viewModel = DashboardViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_exposesDefaultAnalyticsUiState() = runTest(testDispatcher) {
        val state = viewModel.analyticsUiState.value

        assertEquals(TimeWindow.THREE_DAYS, state.selectedTimeWindow)
        assertNull(state.selectedScope)
        assertTrue(state.isGlobal)
        assertEquals(9, state.stageDwellTimes.size)

        // Verify strictly ordered chronological SDLC phases 1 to 9
        val stages = state.stageDwellTimes.map { it.stage }
        assertEquals(
            listOf(
                SdlcStage.DEFINITION,
                SdlcStage.READY_FOR_ARCHITECT,
                SdlcStage.NEEDS_PO_INPUT,
                SdlcStage.REVIEW,
                SdlcStage.ARCHITECT_REWORK,
                SdlcStage.AWAITING_APPROVAL,
                SdlcStage.IN_PROGRESS,
                SdlcStage.PR_REVIEW,
                SdlcStage.DONE
            ),
            stages
        )

        assertEquals(0L, state.totalDwellTimeMillis)
        assertNull(state.bottleneckStage)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun transitionsEmittedFromDao_updatesAnalyticsUiStateAndDwellTimes() = runTest(testDispatcher) {
        val now = Instant.now()
        val t0 = now.minusSeconds(3600 * 24).toString() // 24h ago: definition
        val t1 = now.minusSeconds(3600 * 12).toString() // 12h ago: ready for architect
        val t2 = now.minusSeconds(3600 * 6).toString()  // 6h ago: in-progress

        statusTransitionDao.insertTransitions(
            listOf(
                StatusTransitionEntity(
                    issueId = "owner/repo#1",
                    owner = "owner",
                    repo = "repo",
                    issueNumber = 1,
                    labelName = "status:definition",
                    timestamp = t0
                ),
                StatusTransitionEntity(
                    issueId = "owner/repo#1",
                    owner = "owner",
                    repo = "repo",
                    issueNumber = 1,
                    labelName = "status:ready-for-architect",
                    timestamp = t1
                ),
                StatusTransitionEntity(
                    issueId = "owner/repo#1",
                    owner = "owner",
                    repo = "repo",
                    issueNumber = 1,
                    labelName = "status:in-progress",
                    timestamp = t2
                )
            )
        )

        advanceUntilIdle()

        val state = viewModel.analyticsUiState.value
        assertEquals(9, state.stageDwellTimes.size)
        assertTrue(state.totalDwellTimeMillis > 0L)

        val defStage = state.stageDwellTimes.first { it.stage == SdlcStage.DEFINITION }
        val rfaStage = state.stageDwellTimes.first { it.stage == SdlcStage.READY_FOR_ARCHITECT }
        val progStage = state.stageDwellTimes.first { it.stage == SdlcStage.IN_PROGRESS }

        assertEquals(1, defStage.transitionCount)
        assertEquals(1, rfaStage.transitionCount)
        assertEquals(1, progStage.transitionCount)

        // 12 hours difference between t0 and t1 = 12 * 3600 * 1000 ms
        assertEquals(12 * 3600 * 1000L, defStage.durationMillis)
        // 6 hours difference between t1 and t2 = 6 * 3600 * 1000 ms
        assertEquals(6 * 3600 * 1000L, rfaStage.durationMillis)

        assertNotNull(state.bottleneckStage)
    }

    @Test
    fun setTimeWindow_recomputesAnalyticsWithoutNetworkFetch() = runTest(testDispatcher) {
        val now = Instant.now()
        val tOld = now.minusSeconds(3600 * 24 * 10).toString() // 10 days ago (outside 3D, inside 2W)
        val tRecent = now.minusSeconds(3600 * 12).toString()    // 12 hours ago (inside 3D)

        statusTransitionDao.insertTransitions(
            listOf(
                StatusTransitionEntity(
                    issueId = "owner/repo#1",
                    owner = "owner",
                    repo = "repo",
                    issueNumber = 1,
                    labelName = "status:review",
                    timestamp = tOld
                ),
                StatusTransitionEntity(
                    issueId = "owner/repo#2",
                    owner = "owner",
                    repo = "repo",
                    issueNumber = 2,
                    labelName = "status:definition",
                    timestamp = tRecent
                )
            )
        )

        advanceUntilIdle()

        val initialNetworkCalls = gitHubService.getIssueEventsCallCount

        // Currently on 3 Days: tOld is outside window
        var state = viewModel.analyticsUiState.value
        assertEquals(TimeWindow.THREE_DAYS, state.selectedTimeWindow)
        val reviewStage3D = state.stageDwellTimes.first { it.stage == SdlcStage.REVIEW }
        assertEquals(0, reviewStage3D.transitionCount)

        // Change time window to 2 Weeks
        viewModel.setTimeWindow(TimeWindow.TWO_WEEKS)
        advanceUntilIdle()

        state = viewModel.analyticsUiState.value
        assertEquals(TimeWindow.TWO_WEEKS, state.selectedTimeWindow)
        assertEquals(TimeWindow.TWO_WEEKS, viewModel.selectedTimeWindow.value)

        // Dwell time recomputed: tOld is now within window
        val reviewStage2W = state.stageDwellTimes.first { it.stage == SdlcStage.REVIEW }
        assertEquals(1, reviewStage2W.transitionCount)

        // Assert NO new GitHub network calls were made
        assertEquals(initialNetworkCalls, gitHubService.getIssueEventsCallCount)

        // Change to 1 Month
        viewModel.setTimeWindow(TimeWindow.ONE_MONTH)
        advanceUntilIdle()

        assertEquals(TimeWindow.ONE_MONTH, viewModel.analyticsUiState.value.selectedTimeWindow)
        assertEquals(initialNetworkCalls, gitHubService.getIssueEventsCallCount)
    }

    @Test
    fun setScopeFilter_filtersByProjectWithoutNetworkFetch() = runTest(testDispatcher) {
        val now = Instant.now()
        val t1 = now.minusSeconds(3600 * 10).toString()

        statusTransitionDao.insertTransitions(
            listOf(
                StatusTransitionEntity(
                    issueId = "alpha/repoA#1",
                    owner = "alpha",
                    repo = "repoA",
                    issueNumber = 1,
                    labelName = "status:definition",
                    timestamp = t1
                ),
                StatusTransitionEntity(
                    issueId = "beta/repoB#2",
                    owner = "beta",
                    repo = "repoB",
                    issueNumber = 2,
                    labelName = "status:in-progress",
                    timestamp = t1
                )
            )
        )

        advanceUntilIdle()

        val initialNetworkCalls = gitHubService.getIssueEventsCallCount

        // 1. Global scope: includes both repoA and repoB
        var state = viewModel.analyticsUiState.value
        assertTrue(state.isGlobal)
        val defGlobal = state.stageDwellTimes.first { it.stage == SdlcStage.DEFINITION }
        val progGlobal = state.stageDwellTimes.first { it.stage == SdlcStage.IN_PROGRESS }
        assertEquals(1, defGlobal.transitionCount)
        assertEquals(1, progGlobal.transitionCount)

        // 2. Scope filter to "alpha/repoA"
        viewModel.setScopeFilter("alpha/repoA")
        advanceUntilIdle()

        state = viewModel.analyticsUiState.value
        assertEquals("alpha/repoA", state.selectedScope)
        assertFalse(state.isGlobal)
        val defRepoA = state.stageDwellTimes.first { it.stage == SdlcStage.DEFINITION }
        val progRepoA = state.stageDwellTimes.first { it.stage == SdlcStage.IN_PROGRESS }
        assertEquals(1, defRepoA.transitionCount)
        assertEquals(0, progRepoA.transitionCount)

        // 3. Scope filter to "beta/repoB"
        viewModel.setScopeFilter("beta/repoB")
        advanceUntilIdle()

        state = viewModel.analyticsUiState.value
        assertEquals("beta/repoB", state.selectedScope)
        val defRepoB = state.stageDwellTimes.first { it.stage == SdlcStage.DEFINITION }
        val progRepoB = state.stageDwellTimes.first { it.stage == SdlcStage.IN_PROGRESS }
        assertEquals(0, defRepoB.transitionCount)
        assertEquals(1, progRepoB.transitionCount)

        // 4. Reset scope filter to null (Global)
        viewModel.setScopeFilter(null)
        advanceUntilIdle()

        state = viewModel.analyticsUiState.value
        assertTrue(state.isGlobal)
        val defReset = state.stageDwellTimes.first { it.stage == SdlcStage.DEFINITION }
        val progReset = state.stageDwellTimes.first { it.stage == SdlcStage.IN_PROGRESS }
        assertEquals(1, defReset.transitionCount)
        assertEquals(1, progReset.transitionCount)

        // Zero additional network calls
        assertEquals(initialNetworkCalls, gitHubService.getIssueEventsCallCount)
    }

    @Test
    fun fetchProjectData_fetchesAndCachesTransitions() = runTest(testDispatcher) {
        val now = Instant.now().toString()

        gitHubService.issuesToReturn = listOf(
            IssueDto(
                number = 10,
                title = "User Story 10",
                state = "open",
                labels = listOf(LabelDto("type:user-story", "0052cc")),
                pullRequest = null,
                body = "Story body"
            )
        )

        gitHubService.subIssuesMap[10] = listOf(
            SubIssueDto(
                number = 11,
                title = "Subtask 11",
                state = "open",
                labels = listOf(LabelDto("status:in-progress", "0075ca")),
                body = "Subtask body"
            )
        )

        gitHubService.issueEventsMap[10] = listOf(
            IssueEventDto(
                event = "labeled",
                label = LabelDto("status:ready-for-architect", "fbca04"),
                createdAt = now
            )
        )

        gitHubService.issueEventsMap[11] = listOf(
            IssueEventDto(
                event = "labeled",
                label = LabelDto("status:in-progress", "0075ca"),
                createdAt = now
            )
        )

        projectDao.insertProject(ProjectEntity(id = "owner/repo", owner = "owner", repo = "repo"))
        advanceUntilIdle()

        val cachedTransitions = statusTransitionDao.getAllTransitionsSync()
        assertEquals(2, cachedTransitions.size)

        val state = viewModel.analyticsUiState.value
        val rfa = state.stageDwellTimes.first { it.stage == SdlcStage.READY_FOR_ARCHITECT }
        val inProg = state.stageDwellTimes.first { it.stage == SdlcStage.IN_PROGRESS }
        assertEquals(1, rfa.transitionCount)
        assertEquals(1, inProg.transitionCount)
    }

    @Test
    fun projectHealthAndSorting_worksAlongsideAnalyticsState() = runTest(testDispatcher) {
        viewModel.setSortOption(SortOption.NAME_DESC)
        assertEquals(SortOption.NAME_DESC, viewModel.sortOption.value)

        viewModel.addProject("invalid-format")
        assertEquals("Invalid format. Use owner/repo", viewModel.addProjectError.value)

        viewModel.clearError()
        assertNull(viewModel.addProjectError.value)
    }
}
