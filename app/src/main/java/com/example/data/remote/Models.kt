package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RepoDto(
    val name: String,
    @Json(name = "full_name") val fullName: String,
    val description: String?,
    @Json(name = "stargazers_count") val stargazersCount: Int
)

@JsonClass(generateAdapter = true)
data class IssueDto(
    val number: Int,
    val title: String,
    val state: String,
    val labels: List<LabelDto>,
    @Json(name = "pull_request") val pullRequest: PullRequestMarker?,
    val body: String?
)

@JsonClass(generateAdapter = true)
data class LabelDto(
    val name: String,
    val color: String
)

@JsonClass(generateAdapter = true)
data class PullRequestMarker(
    val url: String
)

@JsonClass(generateAdapter = true)
data class PullRequestDto(
    val number: Int,
    val title: String,
    val state: String,
    val labels: List<LabelDto>,
    val body: String?
)

@JsonClass(generateAdapter = true)
data class UserDto(
    val login: String?
)

@JsonClass(generateAdapter = true)
data class CommentDto(
    val id: Long,
    val body: String,
    val user: UserDto? = null
)

@JsonClass(generateAdapter = true)
data class SubIssueDto(
    val id: Long? = null,
    val number: Int,
    val title: String,
    val state: String = "open",
    val labels: List<LabelDto>? = emptyList(),
    val body: String? = null
)

@JsonClass(generateAdapter = true)
data class IssueEventDto(
    val event: String,
    val label: LabelDto? = null,
    @Json(name = "created_at") val createdAt: String
)
