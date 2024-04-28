package app.minion.shell.view.codeblock

import app.minion.core.functions.TodoistTaskFunctions.Companion.filterByAnyTag
import app.minion.core.functions.TodoistTaskFunctions.Companion.filterBySource
import app.minion.core.functions.TodoistTaskFunctions.Companion.filterByTags
import app.minion.core.model.Tag
import app.minion.core.model.todoist.TodoistTask

interface CodeBlockTodoistIncludeFunctions { companion object {
    fun List<TodoistTask>.applyInclude(include: IncludeExcludeOptions) : List<TodoistTask> {
        return if (include.and.isNotEmpty()) {
            this.applyIncludeAnd(include.and)
        } else if (include.or.isNotEmpty()) {
            this.applyIncludeOr(include.or)
        } else {
            this.applyIncludeAnd(listOf(include))
        }
    }

    fun List<TodoistTask>.applyIncludeAnd(includeList: List<IncludeExcludeOptions>) : List<TodoistTask> {
        var filteredTasks = this
        includeList.forEach { include ->
            filteredTasks = if (include.and.isNotEmpty()) {
                filteredTasks.applyIncludeAnd(include.and)
            } else if (include.or.isNotEmpty()) {
                filteredTasks.applyIncludeOr(include.or)
            } else {
                filteredTasks
                    .applyIncludeTagsAnd(include)
                    .applyIncludeSourceAnd(include)
            }
        }
        return filteredTasks
    }

    fun List<TodoistTask>.applyIncludeOr(includeList: List<IncludeExcludeOptions>) : List<TodoistTask> {
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

    fun List<TodoistTask>.applyIncludeTagsAnd(include: IncludeExcludeOptions) : List<TodoistTask> {
        return if (include.tags.isNotEmpty()) {
            this.filterByTags(include.tags.map { Tag(it) })
        } else {
            this
        }
    }

    fun List<TodoistTask>.applyIncludeTagsOr(include: IncludeExcludeOptions) : List<TodoistTask> {
        return if (include.tags.isNotEmpty()) {
            this.filterByAnyTag(include.tags.map { Tag(it) })
        } else {
            this
        }
    }

    fun List<TodoistTask>.applyIncludeSourceAnd(include: IncludeExcludeOptions) : List<TodoistTask> {
        return if (include.source.isNotEmpty()) {
            this.filterBySource(include.source)
        } else {
            this
        }
    }
}}