package com.example.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GitHubService {
    @GET("repos/{owner}/{repo}")
    suspend fun getRepo(
        @Path("owner") owner: String, 
        @Path("repo") repo: String
    ): RepoDto

    @GET("repos/{owner}/{repo}/issues")
    suspend fun getIssues(
        @Path("owner") owner: String, 
        @Path("repo") repo: String,
        @Query("state") state: String = "open",
        @Query("labels") labels: String? = null,
        @Query("per_page") perPage: Int = 100
    ): List<IssueDto>
    
    @GET("repos/{owner}/{repo}/pulls")
    suspend fun getPullRequests(
        @Path("owner") owner: String, 
        @Path("repo") repo: String,
        @Query("state") state: String = "open",
        @Query("per_page") perPage: Int = 100
    ): List<PullRequestDto>

    @GET("repos/{owner}/{repo}/issues/{issue_number}/comments")
    suspend fun getIssueComments(
        @Path("owner") owner: String, 
        @Path("repo") repo: String,
        @Path("issue_number") issueNumber: Int
    ): List<CommentDto>

    @GET("repos/{owner}/{repo}/issues/{issue_number}/sub_issues")
    suspend fun getSubIssues(
        @Path("owner") owner: String, 
        @Path("repo") repo: String,
        @Path("issue_number") issueNumber: Int,
        @Query("per_page") perPage: Int = 100
    ): List<SubIssueDto>

    @GET("repos/{owner}/{repo}/issues/{issue_number}/events")
    suspend fun getIssueEvents(
        @Path("owner") owner: String, 
        @Path("repo") repo: String,
        @Path("issue_number") issueNumber: Int,
        @Query("per_page") perPage: Int = 100
    ): List<IssueEventDto>

    @GET("repos/{owner}/{repo}/issues/{issue_number}")
    suspend fun getIssue(
        @Path("owner") owner: String, 
        @Path("repo") repo: String,
        @Path("issue_number") issueNumber: Int
    ): IssueDto

    @GET("repos/{owner}/{repo}/pulls/{pull_number}")
    suspend fun getPullRequest(
        @Path("owner") owner: String, 
        @Path("repo") repo: String,
        @Path("pull_number") pullNumber: Int
    ): PullRequestDto
}
