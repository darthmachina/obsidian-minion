package app.minion.shell.view.codeblock

import app.minion.core.model.Task
import app.minion.core.store.MinionStore
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.outputHeading
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.outputTaskStats
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.showError
import app.minion.shell.view.codeblock.CodeBlockTaskFunctions.Companion.applyCodeBlockConfig
import app.minion.shell.view.codeblock.CodeBlockTaskFunctions.Companion.maybeAddProperties
import io.kvision.state.sub
import kotlinx.dom.clear
import kotlinx.html.dom.append
import kotlinx.html.js.div
import mu.KotlinLogging
import org.w3c.dom.HTMLElement

private val logger = KotlinLogging.logger("CodeBlockTaskGalleryView")

interface CodeBlockTaskGalleryView { companion object {
    fun HTMLElement.addTaskGalleryView(config: CodeBlockConfig, store: MinionStore) {
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
                logger.debug { "Task list updated, running updateTasks(): $tasks" }
                this.updateTasks(tasks, store, updatedConfig)
            }
    }

    fun HTMLElement.updateTasks(tasks: List<Task>, store: MinionStore, config: CodeBlockConfig) {
        this.clear()
        if (config.heading.isNotEmpty()) {
            this.outputHeading(config.heading)
        }

        tasks.forEach { task ->
            this.append.div {

            }
        }

        this.outputTaskStats(tasks)
    }
}}