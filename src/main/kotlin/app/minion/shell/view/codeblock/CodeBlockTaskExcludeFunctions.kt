package app.minion.shell.view.codeblock

import app.minion.core.functions.TaskFilterFunctions.Companion.excludeByTags
import app.minion.core.functions.TaskFilterFunctions.Companion.excludeSources
import app.minion.core.model.Tag
import app.minion.core.model.Task
import mu.KotlinLogging

private val logger = KotlinLogging.logger("CodeBlockExcludeFunctions")

interface CodeBlockTaskExcludeFunctions { companion object {
    fun List<Task>.applyExclude(exclude: IncludeExcludeOptions) : List<Task> {
        // If 'and' or 'or' are set, filter accordingly, otherwise assuming a single filter list ANDed together
        return if (exclude.and.isNotEmpty()) {
            this.applyExcludeAnd(exclude.and)
        } else if (exclude.or.isNotEmpty()) {
            this.applyExcludeOr(exclude.or)
        } else {
            this.applyExcludeAnd(listOf(exclude))
        }
    }

    fun List<Task>.applyExcludeAnd(excludeList: List<IncludeExcludeOptions>) : List<Task> {
        var filteredTasks = this
        excludeList.forEach { exclude ->
            filteredTasks = if (exclude.and.isNotEmpty()) {
                filteredTasks.applyExcludeAnd(exclude.and)
            } else if (exclude.or.isNotEmpty()) {
                filteredTasks.applyExcludeOr(exclude.or)
            } else {
                filteredTasks
                    .applyExcludeSourceAnd(exclude)
                    .applyExcludeTagsAnd(exclude)
            }
        }
        return filteredTasks
    }

    fun List<Task>.applyExcludeOr(excludeList: List<IncludeExcludeOptions>) : List<Task> {
        var filteredTasks = this
        excludeList.forEach { exclude ->
            filteredTasks = if (exclude.and.isNotEmpty()) {
                filteredTasks.applyExcludeAnd(exclude.and)
            } else if (exclude.or.isNotEmpty()) {
                filteredTasks.applyExcludeOr(exclude.or)
            } else {
                filteredTasks
            }
        }
        return filteredTasks
    }

    fun List<Task>.applyExcludeSourceAnd(exclude: IncludeExcludeOptions) : List<Task> {
        return if (exclude.source.isNotEmpty()) {
            this.excludeSources(exclude.source)
        } else {
            this
        }
    }

    fun List<Task>.applyExcludeTagsAnd(exclude: IncludeExcludeOptions) : List<Task> {
        return if (exclude.tags.isNotEmpty()) {
            this.excludeByTags(exclude.tags.map { Tag(it) })
        } else {
            this
        }
    }
}}