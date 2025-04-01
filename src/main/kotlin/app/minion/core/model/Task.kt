package app.minion.core.model

import kotlin.uuid.Uuid

import arrow.core.None
import arrow.core.Option
import kotlinx.datetime.Instant

data class Task(
    val content: Content,
    val fileInfo: ListItemFileInfo,
    val tags: Set<Tag> = emptySet(),
    val dueDate: Option<DateTime> = None,
    val hideUntil: Option<DateTime> = None,
    val repeatInfo: Option<RepeatInfo> = None,
    val subtasks: List<Task> = emptyList(),
    val notes: List<Note> = emptyList(),
    val id: Uuid = Uuid.random(),
    val completedOn: Option<Instant> = None,
    val important: Boolean = false,
    val urgent: Boolean = false,
    val createdOn: Option<DateTime> = None,
    val completed: Boolean = false
)
