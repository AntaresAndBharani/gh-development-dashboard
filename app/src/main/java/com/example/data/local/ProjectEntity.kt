package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String, // format: owner/repo
    val owner: String,
    val repo: String,
    val addedAt: Long = System.currentTimeMillis()
)
