package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "status_transitions")
data class StatusTransitionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val issueId: String, // format: owner/repo#number
    val owner: String,
    val repo: String,
    val issueNumber: Int,
    val labelName: String,
    val eventType: String = "labeled",
    val timestamp: String // ISO 8601 format, e.g. 2026-08-27T10:00:00Z
)