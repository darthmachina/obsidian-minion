package app.minion.shell.view.codeblock

import app.minion.core.store.MinionStore
import app.minion.shell.view.Item
import app.minion.shell.view.ViewItems
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.outputGroupLabel
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.outputHeading
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.outputItemStats
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.showError
import app.minion.shell.view.codeblock.CodeBlockTaskFunctions.Companion.applyCodeBlockConfig
import app.minion.shell.view.codeblock.CodeBlockTaskFunctions.Companion.maybeAddProperties
import app.minion.shell.view.codeblock.components.CodeBlockCard.Companion.outputTaskCard
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
            .sub { it.oldtasks.applyCodeBlockConfig(config) }
            .subscribe { tasks ->
                tasks.map {
                    logger.debug { "Task list updated, running updateTasks(): $tasks" }
                    this.updateTasks(it, store, updatedConfig)
                }
            }
    }

    fun HTMLElement.updateTasks(viewItems: List<ViewItems>, store: MinionStore, config: CodeBlockConfig) {
        this.clear()
        if (config.heading.isNotEmpty()) {
            this.outputHeading(config.heading)
        }

        if (viewItems.isNotEmpty()) {
            this.append.div {
                viewItems.forEach { viewItem ->
                    outputGroupDiv(viewItem.group, viewItem.items, config, store)
                }
            }
        }

        this.outputItemStats(viewItems)
    }

    fun FlowContent.outputGroupDiv(label: String, items: List<Item>, config: CodeBlockConfig, store: MinionStore) {
        if (label == GROUP_BY_SINGLE) {
            outputItemList(items, config, store)
        } else {
            div {
                outputGroupLabel(label, store)
                outputItemList(items, config, store)
            }
        }
    }

    fun FlowContent.outputItemList(items: List<Item>, config: CodeBlockConfig, store: MinionStore) {
        div(classes = "mi-codeblock-page-gallery") {
            items.forEach { item ->
                outputTaskCard(item, config, store)
            }
        }
    }
}}
