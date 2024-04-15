package app.minion.shell.view.codeblock

import app.minion.core.MinionError
import app.minion.core.functions.DateTimeFunctions.Companion.asString
import app.minion.core.functions.TaskFilterFunctions.Companion.excludeByTags
import app.minion.core.functions.TaskFilterFunctions.Companion.filterByOverdue
import app.minion.core.functions.TaskFilterFunctions.Companion.filterByToday
import app.minion.core.functions.TaskFilterFunctions.Companion.filterByTodayOrOverdue
import app.minion.core.functions.TaskFilterFunctions.Companion.filterByUpcoming
import app.minion.core.functions.TaskTagFunctions.Companion.collectTags
import app.minion.core.functions.TaskTagFunctions.Companion.findTagWithPrefix
import app.minion.core.model.DateTime
import app.minion.core.model.Tag
import app.minion.core.model.Task
import app.minion.shell.view.Item
import app.minion.shell.view.ItemType
import app.minion.shell.view.Property
import app.minion.shell.view.PropertyType
import app.minion.shell.view.ViewItems
import app.minion.shell.view.codeblock.CodeBlockTaskFunctions.Companion.toItems
import app.minion.shell.view.codeblock.CodeBlockTaskIncludeFunctions.Companion.applyInclude
import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.some
import mu.KotlinLogging

private val logger = KotlinLogging.logger("CodeBlockTaskFunctions")

interface CodeBlockTaskFunctions { companion object {
    fun List<Task>.applyCodeBlockConfig(config: CodeBlockConfig)
    : Either<MinionError, List<ViewItems>> = either {
        this@applyCodeBlockConfig
            .applyDue(config)
            .applyInclude(config.include)
            .applyExcludeTags(config)
            .applySort(config).bind()
            .applyGroupBy(config).bind()
            .toViewItems(config).bind()
    }

    fun Map<String, List<Task>>.toViewItems(config: CodeBlockConfig)
    : Either<MinionError, List<ViewItems>> = either {
        this@toViewItems
            .map { entry ->
                // TODO swap entry.key for display value in config
                ViewItems(entry.key, entry.value.toItems(config).bind())
            }
    }

    fun List<Task>.applyDue(config: CodeBlockConfig) : List<Task> {
        return if (config.due.contains(DueOptions.today) && config.due.contains(DueOptions.overdue)) {
            this.filterByTodayOrOverdue()
        } else if (config.due.contains(DueOptions.today)) {
            this.filterByToday()
        } else if (config.due.contains(DueOptions.overdue)) {
            this.filterByOverdue()
        } else if (config.due.contains(DueOptions.upcoming)) {
            this.filterByUpcoming()
        } else {
            this
        }
    }

    fun List<Task>.applyExcludeTags(config: CodeBlockConfig) : List<Task> {
        return if (config.exclude.tags.isNotEmpty()) {
            this.excludeByTags(config.exclude.tags.map { Tag(it) })
        } else {
            this
        }
    }

    fun List<Task>.applySort(config: CodeBlockConfig) : Either<MinionError, List<Task>> = either {
        if (config.sort.contains(SORT_BY_EISENHOWER)) {
            // Sort by due date then important/urgent
            // 3 -> important && urgent
            // 2 -> urgent
            // 1 -> important
            // 0 -> neither
            this@applySort.sortedWith(
                compareBy<Task, DateTime?>(nullsLast()) { it.dueDate.getOrNull() }
                    .thenByDescending {
                        0 + (if (it.important) 1 else 0) + (if (it.urgent) 2 else 0)
                    }
                    .thenBy {
                        it.content.v
                    }
            )
        } else {
            this@applySort.sortedWith(
                compareBy<Task, DateTime?>(nullsLast()) { it.dueDate.getOrNull() }
                    .thenBy { it.content.v }
            )
        }
    }

    fun List<Task>.applyGroupBy(config: CodeBlockConfig)
    : Either<MinionError, Map<String, List<Task>>> = either {
        if (config.groupBy== GroupByOptions.NONE) {
            mapOf(GROUP_BY_SINGLE to this@applyGroupBy)
        } else {
            when (config.groupBy) {
                GroupByOptions.parent_tag -> {
                    this@applyGroupBy
                        .applyGroupByForParentTag(config).bind()
                }
                GroupByOptions.source -> {
                    this@applyGroupBy
                        .applyGroupByForSource().bind()
                }
                else -> raise(MinionError.ConfigError("${config.groupBy} not implemented yet"))
            }
        }
    }

    fun List<Task>.applyGroupByForParentTag(config: CodeBlockConfig)
    : Either<MinionError, Map<String, List<Task>>> = either {
        if (config.groupByField.isEmpty()) {
            raise(MinionError.ConfigError("No groupByField specified for groupBy option"))
        }

        this@applyGroupByForParentTag
            .groupBy { task ->
                task
                    .findTagWithPrefix(config.groupByField)
                    .map { it.v.replace(config.groupByField, "") }
                    .getOrElse {
                        logger.warn { "No grouping found for ${task.content.v}, using Unknown\n$it" }
                        GROUP_BY_UNKNOWN
                    }

            }
    }

    fun List<Task>.applyGroupByForSource()
    : Either<MinionError, Map<String, List<Task>>> = either {
        this@applyGroupByForSource
            .groupBy { task ->
                task.fileInfo.file.v
            }
    }

    fun List<Task>.toItems(config: CodeBlockConfig) : Either<MinionError, List<Item>> = either {
        this@toItems
            .map { task ->
                Item(
                    ItemType.TASK,
                    task.content,
                    task.toPropertyList(config).bind(),
                    task = task.some()
                )
            }
    }

    fun Task.toPropertyList(config: CodeBlockConfig) : Either<MinionError, List<Property>> = either {
        config.properties.flatMap { configProperty ->
            when (configProperty) {
                PROPERTY_TAGS -> {
                    listOf(
                        Property(
                            PropertyType.TAGS,
                            "Tags",
                            this@toPropertyList.collectTags().joinToString(" ") { it.v }
                        )
                    )
                }
                PROPERTY_SOURCE -> {
                    listOf(
                        Property(
                            PropertyType.SOURCE,
                            "Source",
                            this@toPropertyList.fileInfo.file.v
                        )
                    )
                }
                PROPERTY_DUE -> {
                    listOf(
                        Property(
                            PropertyType.DUE,
                            "Due",
                            this@toPropertyList.dueDate.map { it.asString() }.getOrElse { "-" }
                        )
                    )
                }
                PROPERTY_EISENHOWER -> TODO()
                else -> TODO()
            }
        }
    }

    fun Set<Tag>.removeConfigTags(config: CodeBlockConfig) : Set<Tag> {
        return filter { tag ->
            !(tag.v == "task" || config.include.tags.contains(tag.v))
        }.toSet()
    }

    fun CodeBlockConfig.maybeAddProperties() : CodeBlockConfig {
        return if (this.properties.isEmpty()) {
            this.copy(
                properties = listOf(PROPERTY_DUE, PROPERTY_SOURCE, PROPERTY_TAGS, PROPERTY_EISENHOWER)
            )
        } else {
            this
        }
    }
}}
