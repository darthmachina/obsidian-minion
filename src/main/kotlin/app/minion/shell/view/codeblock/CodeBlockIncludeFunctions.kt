package app.minion.shell.view.codeblock

import app.minion.core.functions.TaskFilterFunctions.Companion.filterByAnyTag
import app.minion.core.functions.TaskFilterFunctions.Companion.filterByTags
import app.minion.core.model.Tag
import app.minion.core.model.Task
import app.minion.shell.view.codeblock.CodeBlockIncludeFunctions.Companion.applyIncludeAnd
import mu.KotlinLogging

private val logger = KotlinLogging.logger("CodeBlockIncludeFunctions")

interface CodeBlockIncludeFunctions { companion object {
    /**
     * Root filter function, calls other functions as needed.
     */
    fun List<Task>.applyInclude(include: IncludeExcludeOptions) : List<Task> {
        // If 'and' or 'or' are set, filter accordingly, otherwise assuming a single filter list ANDed together
        return if (include.and.isNotEmpty()) {
            this.applyIncludeAnd(include.and)
        } else if (include.or.isNotEmpty()) {
            this.applyIncludeOr(include.or)
        } else {
            this.applyIncludeAnd(listOf(include))
        }
    }

    fun List<Task>.applyIncludeAnd(includeList: List<IncludeExcludeOptions>) : List<Task> {
        var filteredTasks = this
        includeList.forEach { include ->
            filteredTasks = if (include.and.isNotEmpty()) {
                filteredTasks.applyIncludeAnd(include.and)
            } else if (include.or.isNotEmpty()) {
                filteredTasks.applyIncludeOr(include.or)
            } else {
                filteredTasks.applyIncludeTagsAnd(include)
            }
        }
        return filteredTasks
    }

    fun List<Task>.applyIncludeOr(includeList: List<IncludeExcludeOptions>) : List<Task> {
        var filteredTasks = this
        includeList.forEach { include ->
            filteredTasks = if (include.and.isNotEmpty()) {
                filteredTasks.applyIncludeAnd(include.and)
            } else if (include.or.isNotEmpty()) {
                filteredTasks.applyIncludeOr(include.or)
            } else {
                filteredTasks.applyIncludeTagsOr(include)
            }
        }
        return filteredTasks
    }

    fun List<Task>.applyIncludeTagsAnd(include: IncludeExcludeOptions) : List<Task> {
        return if (include.tags.isNotEmpty()) {
            this.filterByTags(include.tags.map { Tag(it) })
        } else {
            this
        }
    }

    fun List<Task>.applyIncludeTagsOr(include: IncludeExcludeOptions) : List<Task> {
        return if (include.tags.isNotEmpty()) {
            this.filterByAnyTag(include.tags.map { Tag(it) })
        } else {
            this
        }
    }
}}
