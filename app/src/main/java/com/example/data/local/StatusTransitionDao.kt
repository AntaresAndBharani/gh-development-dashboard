package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StatusTransitionDao {
    @Query("SELECT * FROM status_transitions ORDER BY timestamp ASC")
    fun getAllTransitions(): Flow<List<StatusTransitionEntity>>

    @Query("SELECT * FROM status_transitions ORDER BY timestamp ASC")
    suspend fun getAllTransitionsSync(): List<StatusTransitionEntity>

    @Query("SELECT * FROM status_transitions WHERE owner = :owner AND repo = :repo ORDER BY timestamp ASC")
    fun getTransitionsForRepo(owner: String, repo: String): Flow<List<StatusTransitionEntity>>

    @Query("SELECT * FROM status_transitions WHERE owner = :owner AND repo = :repo ORDER BY timestamp ASC")
    suspend fun getTransitionsForRepoSync(owner: String, repo: String): List<StatusTransitionEntity>

    @Query("SELECT * FROM status_transitions WHERE issueId = :issueId ORDER BY timestamp ASC")
    fun getTransitionsForIssue(issueId: String): Flow<List<StatusTransitionEntity>>

    @Query("SELECT * FROM status_transitions WHERE issueId = :issueId ORDER BY timestamp ASC")
    suspend fun getTransitionsForIssueSync(issueId: String): List<StatusTransitionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransitions(transitions: List<StatusTransitionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransition(transition: StatusTransitionEntity)

    @Query("DELETE FROM status_transitions WHERE issueId = :issueId")
    suspend fun deleteTransitionsForIssue(issueId: String)

    @Query("DELETE FROM status_transitions WHERE owner = :owner AND repo = :repo")
    suspend fun deleteTransitionsForRepo(owner: String, repo: String)

    @Query("DELETE FROM status_transitions")
    suspend fun clearAll()
}