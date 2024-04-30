package app.minion.core.functions

import app.minion.core.model.Tag
import app.minion.core.model.todoist.TodoistTask
import arrow.core.getOrElse

interface TodoistTaskFunctions { companion object {
    fun List<TodoistTask>.getRootTasks() : List<TodoistTask> {
        return this
            .filter { it.parentId.isNone() }
            .map { task ->
                task.copy(
                    subtasks = this.filter { it.parentId.map { it == task.id }.getOrElse { false } }
                )
            }
    }

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

    /**
     * Takes a list to maintain consistency with other filter methods, but sections are mutually exclusive so only
     * operates on the first in the list
     */
    fun List<TodoistTask>.filterBySection(sections: List<String>) : List<TodoistTask> {
        return this
            .filter { task ->
                task.section.map { it.name == sections.first() }.getOrElse { false }
            }
    }

    fun List<TodoistTask>.filterByAnySection(sections: List<String>) : List<TodoistTask> {
        return this
            .filter { task ->
                task.section
                    .map {
                        sections.contains(it.name)
                    }
                    .getOrElse { false }
            }
    }
}}
