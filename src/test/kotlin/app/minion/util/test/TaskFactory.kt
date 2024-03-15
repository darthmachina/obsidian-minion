package app.minion.util.test

import app.minion.core.model.*

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
}}
