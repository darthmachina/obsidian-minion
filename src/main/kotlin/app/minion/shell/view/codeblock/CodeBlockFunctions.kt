package app.minion.shell.view.codeblock

import app.minion.core.functions.TaskFilterFunctions.Companion.excludeByTags
import app.minion.core.functions.TaskFilterFunctions.Companion.filterByOverdue
import app.minion.core.functions.TaskFilterFunctions.Companion.filterByTags
import app.minion.core.functions.TaskFilterFunctions.Companion.filterByToday
import app.minion.core.functions.TaskFilterFunctions.Companion.filterByTodayOrOverdue
import app.minion.core.model.Tag
import app.minion.core.model.Task

interface CodeBlockFunctions { companion object {
    fun List<Task>.applyDue(config: CodeBlockConfig) : List<Task> {
        return if (config.due.contains(DueOptions.overdue) && config.due.contains(DueOptions.overdue)) {
            this.filterByTodayOrOverdue()
        } else if (config.due.contains(DueOptions.today)) {
            this.filterByToday()
        } else if (config.due.contains(DueOptions.overdue)) {
            this.filterByOverdue()
        } else {
            this
        }
    }

    fun List<Task>.applyIncludeTags(config: CodeBlockConfig) : List<Task> {
        return if (config.include.tags.isNotEmpty()) {
            this.filterByTags(config.include.tags)
        } else {
            this
        }
    }

    fun List<Task>.applyExcludeTags(config: CodeBlockConfig) : List<Task> {
        return if (config.exclude.tags.isNotEmpty()) {
            this.excludeByTags(config.exclude.tags)
        } else {
            this
        }
    }

    fun Set<Tag>.removeConfigTags(config: CodeBlockConfig) : Set<Tag> {
        return filter { tag ->
            !(tag.v == "task" || config.include.tags.contains(tag))
        }.toSet()
    }
}}
