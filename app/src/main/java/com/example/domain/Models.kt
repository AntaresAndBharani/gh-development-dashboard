package com.example.domain

import com.example.data.remote.LabelDto

data class Subtask(
    val number: Int? = null,
    val title: String,
    val state: String = "open",
    val isCompleted: Boolean = false,
    val labels: List<LabelDto> = emptyList(),
    val body: String? = null,
    val linkedNumber: Int? = null
)

data class UserStory(
    val number: Int,
    val title: String,
    val state: String,
    val labels: List<LabelDto>,
    val body: String? = null,
    val subtasks: List<Subtask> = emptyList()
)

data class PullRequest(
    val number: Int,
    val title: String,
    val state: String,
    val labels: List<LabelDto>,
    val body: String?
)

data class ProjectHealth(
    val id: String, // owner/repo
    val owner: String,
    val repo: String,
    val userStories: List<UserStory>,
    val pullRequests: List<PullRequest>,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val openStoriesCount: Int get() = userStories.size
    val openPRsCount: Int get() = pullRequests.size
    
    val totalSubtasks: Int get() = userStories.sumOf { it.subtasks.size }
    val completedSubtasks: Int get() = userStories.sumOf { it.subtasks.count { sub -> sub.isCompleted } }
}

fun parseSubtasks(body: String?): List<Subtask> {
    if (body == null) return emptyList()
    val list = mutableListOf<Subtask>()
    
    val checkboxRegex = Regex("""(?m)^\s*[-*]\s*\[([ xX])\]\s*(.*)$""")
    val numberRegex = Regex("""#(\d+)""")
    val issueUrlRegex = Regex("""/issues/(\d+)""")
    val prUrlRegex = Regex("""/pull/(\d+)""")
    
    checkboxRegex.findAll(body).forEach { match ->
        val isCompleted = match.groupValues[1].lowercase() == "x"
        val rawContent = match.groupValues[2].trim()
        
        val issueNum = numberRegex.find(rawContent)?.groupValues?.get(1)?.toIntOrNull()
            ?: issueUrlRegex.find(rawContent)?.groupValues?.get(1)?.toIntOrNull()
            ?: prUrlRegex.find(rawContent)?.groupValues?.get(1)?.toIntOrNull()
            
        val cleanTitle = rawContent
            .replace(Regex("""\[(.*?)\]\(.*?\)"""), "$1")
            .trim()
            
        list.add(
            Subtask(
                number = issueNum,
                title = if (cleanTitle.isNotBlank()) cleanTitle else (if (issueNum != null) "#$issueNum" else "Subtask"),
                state = if (isCompleted) "closed" else "open",
                isCompleted = isCompleted,
                labels = emptyList(),
                body = null,
                linkedNumber = issueNum
            )
        )
    }
    return list
}
