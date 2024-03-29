package app.minion.shell.view.codeblock

import app.minion.core.MinionError
import app.minion.core.model.Task
import app.minion.core.store.MinionStore
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.outputGroupLabel
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.outputHeading
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.outputTaskStats
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.showError
import app.minion.shell.view.codeblock.CodeBlockTaskFunctions.Companion.applyCodeBlockConfig
import app.minion.shell.view.codeblock.CodeBlockTaskFunctions.Companion.maybeAddProperties
import app.minion.shell.view.codeblock.components.CodeBlockCard.Companion.outputTaskCard
import arrow.core.toOption
import io.kvision.state.sub
import kotlinx.dom.clear
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.js.div
import mu.KotlinLogging
import org.w3c.dom.HTMLElement

private val logger = KotlinLogging.logger("CodeBlockTaskGalleryView")

interface CodeBlockTaskGalleryView { companion object {
    fun HTMLElement.addTaskGalleryView(config: CodeBlockConfig, store: MinionStore) {
        logger.debug { "addTaskGalleryView" }
        classList.add("mi-codeblock")
        store
            .sub { it.error }
            .subscribe { error ->
                error.map { this.showError(it) }
            }
        val updatedConfig = config.maybeAddProperties()
        store
            .sub { it.tasks.applyCodeBlockConfig(config) }
            .subscribe { tasks ->
                tasks.map {
                    logger.debug { "Task list updated, running updateTasks(): $tasks" }
                    this.updateTasks(it, store, updatedConfig)
                }
            }
    }

    fun HTMLElement.updateTasks(tasks: Map<String, List<Task>>, store: MinionStore, config: CodeBlockConfig) {
        this.clear()
        if (config.heading.isNotEmpty()) {
            this.outputHeading(config.heading)
        }

        if (tasks.isNotEmpty()) {
            this.append.div {
                if (config.groupByOrder.isEmpty()) {
                    tasks.forEach { entry ->
                        outputGroup(entry.key, entry.value, config, store)
                    }
                } else {
                    config.groupByOrder.forEach { group ->
                        tasks[group]
                            .toOption().toEither {
                                MinionError.GroupByNotFoundError("$group not found in results")
                            }
                            .map { outputGroup(group, it, config, store) }
                            .mapLeft { logger.warn { "$it" } }
                    }
                }
            }
        }

        this.outputTaskStats(tasks)
    }

    fun FlowContent.outputGroup(label: String, tasks: List<Task>, config: CodeBlockConfig, store: MinionStore) {
        if (label == GROUP_BY_SINGLE) {
            outputTaskList(tasks, config, store)
        } else {
            div {
                outputGroupLabel(label)
                outputTaskList(tasks, config, store)
            }
        }
    }

    fun FlowContent.outputTaskList(tasks: List<Task>, config: CodeBlockConfig, store: MinionStore) {
        div(classes = "mi-codeblock-page-gallery") {
            tasks.forEach { task ->
                outputTaskCard(task, config, store)
            }
        }
    }
}}
