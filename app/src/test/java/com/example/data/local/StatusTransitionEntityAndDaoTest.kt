package com.example.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StatusTransitionEntityAndDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: StatusTransitionDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.statusTransitionDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndQueryTransitionsForIssue() = runTest {
        val transition1 = StatusTransitionEntity(
            issueId = "owner/repo#1",
            owner = "owner",
            repo = "repo",
            issueNumber = 1,
            labelName = "status:definition",
            eventType = "labeled",
            timestamp = "2026-08-27T08:00:00Z"
        )
        val transition2 = StatusTransitionEntity(
            issueId = "owner/repo#1",
            owner = "owner",
            repo = "repo",
            issueNumber = 1,
            labelName = "status:ready-for-architect",
            eventType = "labeled",
            timestamp = "2026-08-27T10:00:00Z"
        )

        dao.insertTransitions(listOf(transition1, transition2))

        val transitions = dao.getTransitionsForIssueSync("owner/repo#1")
        assertEquals(2, transitions.size)
        assertEquals("status:definition", transitions[0].labelName)
        assertEquals("status:ready-for-architect", transitions[1].labelName)
        assertEquals("2026-08-27T08:00:00Z", transitions[0].timestamp)
        assertEquals("2026-08-27T10:00:00Z", transitions[1].timestamp)
    }

    @Test
    fun queryTransitionsForRepoAndAllFlow() = runTest {
        val t1 = StatusTransitionEntity(
            issueId = "owner/repo1#1",
            owner = "owner",
            repo = "repo1",
            issueNumber = 1,
            labelName = "status:in-progress",
            timestamp = "2026-08-27T09:00:00Z"
        )
        val t2 = StatusTransitionEntity(
            issueId = "owner/repo2#2",
            owner = "owner",
            repo = "repo2",
            issueNumber = 2,
            labelName = "status:done",
            timestamp = "2026-08-27T12:00:00Z"
        )

        dao.insertTransitions(listOf(t1, t2))

        val repo1Transitions = dao.getTransitionsForRepo("owner", "repo1").first()
        assertEquals(1, repo1Transitions.size)
        assertEquals("owner/repo1#1", repo1Transitions[0].issueId)

        val allTransitions = dao.getAllTransitions().first()
        assertEquals(2, allTransitions.size)
    }

    @Test
    fun deleteTransitionsForIssue() = runTest {
        val t1 = StatusTransitionEntity(
            issueId = "owner/repo#1",
            owner = "owner",
            repo = "repo",
            issueNumber = 1,
            labelName = "status:definition",
            timestamp = "2026-08-27T08:00:00Z"
        )
        val t2 = StatusTransitionEntity(
            issueId = "owner/repo#2",
            owner = "owner",
            repo = "repo",
            issueNumber = 2,
            labelName = "status:in-progress",
            timestamp = "2026-08-27T09:00:00Z"
        )

        dao.insertTransitions(listOf(t1, t2))
        dao.deleteTransitionsForIssue("owner/repo#1")

        val issue1Transitions = dao.getTransitionsForIssueSync("owner/repo#1")
        val issue2Transitions = dao.getTransitionsForIssueSync("owner/repo#2")

        assertTrue(issue1Transitions.isEmpty())
        assertEquals(1, issue2Transitions.size)
    }

    @Test
    fun clearAllAndRepoDeletion() = runTest {
        val t1 = StatusTransitionEntity(
            issueId = "owner/repo#1",
            owner = "owner",
            repo = "repo",
            issueNumber = 1,
            labelName = "status:definition",
            timestamp = "2026-08-27T08:00:00Z"
        )
        dao.insertTransition(t1)
        assertEquals(1, dao.getAllTransitionsSync().size)

        dao.deleteTransitionsForRepo("owner", "repo")
        assertEquals(0, dao.getAllTransitionsSync().size)

        dao.insertTransition(t1)
        dao.clearAll()
        assertEquals(0, dao.getAllTransitionsSync().size)
    }
}