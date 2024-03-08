package app.minion.core.functions

import app.minion.core.functions.DateTimeFunctions.Companion.isInPast
import app.minion.core.functions.DateTimeFunctions.Companion.isToday
import app.minion.core.functions.DateTimeFunctions.Companion.isTodayOrOverdue
import app.minion.core.functions.DateTimeFunctions.Companion.toLocalDateTime
import app.minion.core.model.Tag
import app.minion.core.model.Task
import arrow.core.getOrElse

interface TaskFilterFunctions { companion object {
    fun List<Task>.filterByTodayOrOverdue() : List<Task> {
        return this
            .filter { task ->
                task.dueDate
                    .map { it.isTodayOrOverdue() }
                    .getOrElse { false }
            }
            .sortedBy { task ->
                task.dueDate.map { it.toLocalDateTime() }.getOrNull()
            }
    }

    fun List<Task>.filterByToday() : List<Task> {
        return this
            .filter { task ->
                task.dueDate
                    .map { it.isToday() }
                    .getOrElse { false }
            }
            .sortedBy { task ->
                task.dueDate.map { it.toLocalDateTime() }.getOrNull()
            }
    }

    fun List<Task>.filterByOverdue() : List<Task> {
        return this
            .filter { task ->
                task.dueDate
                    .map { it.isInPast() }
                    .getOrElse { false }
            }
            .sortedBy { task ->
                task.dueDate.map { it.toLocalDateTime() }.getOrNull()
            }
    }

    fun List<Task>.filterByTags(tags: List<Tag>) : List<Task> {
        return this
            .filter { task ->
                task.tags.containsAll(tags)
            }
    }

    fun List<Task>.excludeByTags(tags: Iterable<Tag>) : List<Task> {
        return this
            .filter { task ->
                !task.tags.any {
                    tags.contains(it)
                }
            }
    }
}}