package app.minion.core.functions

import app.minion.core.functions.DateTimeFunctions.Companion.isInPast
import app.minion.core.functions.DateTimeFunctions.Companion.isToday
import app.minion.core.functions.DateTimeFunctions.Companion.isTodayOrOverdue
import app.minion.core.functions.DateTimeFunctions.Companion.isUpcoming
import app.minion.core.functions.DateTimeFunctions.Companion.toLocalDateTime
import app.minion.core.functions.TaskFilterFunctions.Companion.filterByTags
import app.minion.core.model.Tag
import app.minion.core.model.Task
import arrow.core.getOrElse
import mu.KotlinLogging

private val logger = KotlinLogging.logger("TaskFilterFunctions")

interface TaskFilterFunctions { companion object {
    fun List<Task>.filterByTodayOrOverdue() : List<Task> {
        logger.debug { "filterByTodayOrOverdue()" }
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
        logger.debug { "filterByToday()" }
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
        logger.debug { "filterByOverdue()" }
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

    fun List<Task>.filterByUpcoming() : List<Task> {
        logger.debug { "filterByUpcoming()" }
        return this
            .filter { task ->
                task.dueDate
                    .map { it.isUpcoming() }
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

    fun List<Task>.filterByAnyTag(tags: List<Tag>) : List<Task> {
        return this
            .filter { task ->
                task.tags.any { tags.contains(it) }
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
