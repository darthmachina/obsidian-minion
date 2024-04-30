package app.minion.shell.view.codeblock

import app.minion.core.MinionError
import app.minion.core.functions.DateTimeFunctions.Companion.asString
import app.minion.core.functions.DateTimeFunctions.Companion.isInPast
import app.minion.core.functions.TodoistTaskFunctions.Companion.getRootTasks
import app.minion.core.model.todoist.TodoistTask
import app.minion.shell.view.Item
import app.minion.shell.view.ItemType
import app.minion.shell.view.Property
import app.minion.shell.view.PropertyType
import app.minion.shell.view.ViewItems
import app.minion.shell.view.codeblock.CodeBlockTodoistIncludeFunctions.Companion.applyInclude
import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.some
import mu.KotlinLogging

private val logger = KotlinLogging.logger("CodeBlockTodoistFunctions")

interface CodeBlockTodoistFunctions { companion object {
    fun List<TodoistTask>.applyCodeBlockConfig(config: CodeBlockConfig)
    : Either<MinionError, List<ViewItems>> = either {
        logger.debug { "applyCodeBlockConfig() list size: $size" }
        this@applyCodeBlockConfig
            .getRootTasks()
            .applyInclude(config.include)
            .applySort(config).bind()
            .applyGroupBy(config).bind()
            .toViewItems(config).bind()
    }

    fun Map<String, List<TodoistTask>>.toViewItems(config: CodeBlockConfig)
    : Either<MinionError, List<ViewItems>> = either {
        this@toViewItems
            .map { entry ->
                ViewItems(entry.key, entry.value.toItems(config).bind())
            }
    }

    fun List<TodoistTask>.applySort(config: CodeBlockConfig) : Either<MinionError, List<TodoistTask>> = either {
        logger.debug { "applySort() list size: $size" }
        if (config.sort.contains(SORT_BY_PRIORITY)) {
            this@applySort.sortedBy { it.priority }
        } else {
            this@applySort
        }
    }

    fun List<TodoistTask>.applyGroupBy(config: CodeBlockConfig)
    : Either<MinionError, Map<String, List<TodoistTask>>> = either {
        if (config.groupBy == GroupByOptions.NONE) {
            mapOf(GROUP_BY_SINGLE to this@applyGroupBy)
        } else {
            when (config.groupBy) {
                GroupByOptions.section -> {
                    this@applyGroupBy.applyGroupByForSection().bind()
                }
                else -> raise(MinionError.ConfigError("${config.groupBy} not implemented yet"))
            }
        }
    }

    fun List<TodoistTask>.applyGroupByForSection() : Either<MinionError, Map<String, List<TodoistTask>>> = either {
        this@applyGroupByForSection
            .groupBy { task ->
                task.section.map { it.name }.getOrElse { GROUP_BY_UNKNOWN }
            }
    }

    fun List<TodoistTask>.toItems(config: CodeBlockConfig) : Either<MinionError, List<Item>> = either {
        this@toItems
            .map { task ->
                Item(
                    ItemType.TASK,
                    task.content,
                    task.toPropertyList(config).bind(),
                    todoist = task.some()
                )
            }
    }

    fun TodoistTask.toPropertyList(config: CodeBlockConfig) : Either<MinionError, List<Property>> = either {
        config.properties.flatMap { configProperty ->
            when (configProperty) {
                PROPERTY_TAGS -> {
                    listOf(
                        Property(
                            PropertyType.TAGS,
                            "Labels",
                            this@toPropertyList.labels.joinToString(" ") { it.v }
                        )
                    )
                }
                PROPERTY_SOURCE -> {
                    listOf(
                        Property(
                            PropertyType.SOURCE,
                            "Project",
                            this@toPropertyList.project.name
                        )
                    )
                }
                PROPERTY_SECTION -> {
                    listOf(
                        Property(
                            PropertyType.SECTION,
                            "Section",
                            this@toPropertyList.section.map { it.name }.getOrElse { "None" }
                        )
                    )
                }
                PROPERTY_DUE -> {
                    this@toPropertyList.due.map { dueDate ->
                        listOf(
                            Property(
                                PropertyType.DUE,
                                "Due",
                                dueDate.asString()
                            ),
                            Property(
                                PropertyType.DUE_IN_PAST,
                                "Due in Past",
                                "${dueDate.isInPast()}"
                            )
                        )
                    }.getOrElse { emptyList() }
                }
                else -> TODO("$configProperty not implemented")
            }
        }
    }
}}
