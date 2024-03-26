package app.minion.shell.view.codeblock

import app.minion.core.functions.TaskFilterFunctions.Companion.filterByAnyTag
import app.minion.core.functions.TaskFilterFunctions.Companion.filterByTags
import app.minion.core.model.Tag
import app.minion.core.model.Task
import app.minion.shell.view.codeblock.CodeBlockIncludeFunctions.Companion.applyIncludeAnd

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

    fun List<Task>.applyIncludeAnd(include: List<IncludeExcludeOptions>) : List<Task> {
        var filteredTasks = this
        include.forEach { include ->
            filteredTasks = if (include.and.isNotEmpty()) {
                this.applyIncludeAnd(include.and)
            } else if (include.or.isNotEmpty()) {
                this.applyIncludeOr(include.or)
            } else {
                this.applyIncludeTagsAnd(include)
            }
        }
        return filteredTasks
    }

    fun List<Task>.applyIncludeOr(include: List<IncludeExcludeOptions>) : List<Task> {
        var filteredTasks = this
        include.forEach { include ->
            filteredTasks = if (include.and.isNotEmpty()) {
                this.applyIncludeAnd(include.and)
            } else if (include.or.isNotEmpty()) {
                this.applyIncludeOr(include.or)
            } else {
                this.applyIncludeTagsOr(include)
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
