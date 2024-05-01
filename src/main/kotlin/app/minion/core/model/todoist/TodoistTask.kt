package app.minion.core.model.todoist

import app.minion.core.model.Content
import app.minion.core.model.DateTime
import app.minion.core.model.Tag
import arrow.core.None
import arrow.core.Option

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
    val content: Content,
    val project: Project,
    val description: String,
    val section: Option<Section>,
    val due: Option<DateTime>,
    val priority: Priority,
    val labels: Set<Tag>,
    val parentId: Option<String> = None,
    val subtasks: List<TodoistTask> = emptyList()
)
