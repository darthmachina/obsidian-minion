package app.minion.shell.view.codeblock

import app.minion.core.functions.TaskFilterFunctions.Companion.filterByOverdue
import app.minion.core.functions.TaskFilterFunctions.Companion.filterByToday
import app.minion.core.functions.TaskFilterFunctions.Companion.filterByTodayOrOverdue
import app.minion.core.functions.TaskFilterFunctions.Companion.filterByUpcoming
import app.minion.core.functions.TodoistTaskFunctions.Companion.filterByAnySection
import app.minion.core.functions.TodoistTaskFunctions.Companion.filterByAnyTag
import app.minion.core.functions.TodoistTaskFunctions.Companion.filterByOverdue
import app.minion.core.functions.TodoistTaskFunctions.Companion.filterBySection
import app.minion.core.functions.TodoistTaskFunctions.Companion.filterBySource
import app.minion.core.functions.TodoistTaskFunctions.Companion.filterByTags
import app.minion.core.functions.TodoistTaskFunctions.Companion.filterByToday
import app.minion.core.functions.TodoistTaskFunctions.Companion.filterByTodayOrOverdue
import app.minion.core.functions.TodoistTaskFunctions.Companion.filterByUpcoming
import app.minion.core.model.Tag
import app.minion.core.model.todoist.TodoistTask
import app.minion.shell.view.codeblock.CodeBlockTaskFunctions.Companion.applyDue

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
                    .applyIncludeSectionAnd(include)
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
                filteredTasks
                    .applyIncludeTagsOr(include)
                    .applyIncludeSectionOr(include)
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

    fun List<TodoistTask>.applyIncludeSectionAnd(include: IncludeExcludeOptions) : List<TodoistTask> {
        return if (include.section.isNotEmpty()) {
            this.filterBySection(include.section)
        } else {
            this
        }
    }

    fun List<TodoistTask>.applyIncludeSectionOr(include: IncludeExcludeOptions) : List<TodoistTask> {
        return if (include.section.isNotEmpty()) {
            this.filterByAnySection(include.section)
        } else {
            this
        }
    }
}}
