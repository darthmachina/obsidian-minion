package app.minion.core.model.todoist

enum class Priority {
    ONE,
    TWO,
    THREE,
    FOUR
}

enum class DurationUnit {
    MINUTE,
    DAY
}

data class Duration(
    val duration: Int,
    val unit: DurationUnit
)

data class TodoistTask(
    val id: String,
    val content: String,
    val project: Project,
    val description: String,
    val due: String, // TODO Convert to Date object
    val priority: Priority,
    val labels: List<String>,
    val subtasks: List<TodoistTask> = emptyList()
)
