package app.minion.util.test

import app.minion.core.model.*
import app.minion.core.model.todoist.Priority
import app.minion.core.model.todoist.Project
import app.minion.core.model.todoist.TodoistTask
import arrow.core.None

interface TaskFactory { companion object {
    fun createBasicTask() : Task {
        return Task(
            content = Content("Test task"),
            tags = setOf(Tag("task")),
            fileInfo = ListItemFileInfo(Filename("test"), File("test.md"), 1, "- [ ] Test task #task")
        )
    }

    fun createTaskWithTag(tag: Tag) : Task {
        return createBasicTask().let {
            it.copy(tags = it.tags.plus(tag))
        }
    }

    fun createBasicTodoistTask() : TodoistTask {
        return TodoistTask(
            id = "id",
            content = Content("Test task"),
            project = Project("projectId", "Test Project", "green"),
            description = "Description",
            section = None,
            due = None,
            priority = Priority.FOUR,
            labels = emptySet(),
        )
    }
}}
