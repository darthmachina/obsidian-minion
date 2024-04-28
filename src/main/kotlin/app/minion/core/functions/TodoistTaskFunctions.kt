package app.minion.core.functions

import app.minion.core.model.Tag
import app.minion.core.model.todoist.TodoistTask

interface TodoistTaskFunctions { companion object {
    fun List<TodoistTask>.filterByTags(tags: List<Tag>) : List<TodoistTask> {
        return this
            .filter { task ->
                task.labels.containsAll(tags)
            }
    }

    fun List<TodoistTask>.filterByAnyTag(tags: List<Tag>) : List<TodoistTask> {
        return this
            .filter { task ->
                task.labels.any { tags.contains(it) }
            }
    }

    fun List<TodoistTask>.filterBySource(sources: List<String>) : List<TodoistTask> {
        return this
            .filter { task ->
                sources.all { search ->
                    search == task.project.name
                }
            }
    }
}}