package app.minion.shell.view.codeblock

import app.minion.core.model.Tag
import app.minion.core.store.MinionStore
import app.minion.shell.view.Item
import app.minion.shell.view.PropertyType
import app.minion.shell.view.ViewFunctions.Companion.outputCheckbox
import app.minion.shell.view.ViewFunctions.Companion.outputDue
import app.minion.shell.view.ViewFunctions.Companion.outputStyledContent
import app.minion.shell.view.ViewItems
import app.minion.shell.view.ViewModelFunctions.Companion.getPropertyValue
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.outputGroupLabel
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.outputHeading
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.outputItemStats
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.showError
import app.minion.shell.view.codeblock.CodeBlockTaskListView.Companion.outputSource
import app.minion.shell.view.codeblock.CodeBlockTaskListView.Companion.outputTags
import app.minion.shell.view.codeblock.CodeBlockTodoistFunctions.Companion.applyCodeBlockConfig
import arrow.core.getOrElse
import io.kvision.state.sub
import kotlinx.dom.clear
import kotlinx.dom.isElement
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.span
import mu.KotlinLogging
import org.w3c.dom.HTMLElement

private val logger = KotlinLogging.logger("CodeBlockTodoistListView")

interface CodeBlockTodoistListView { companion object {
    fun HTMLElement.addTodoistListView(config: CodeBlockConfig, store: MinionStore) {
        classList.add("mk-codeblock")
        store
            .sub { it.error }
            .subscribe { error ->
                error.map { showError(it) }
            }

        store
            .sub { it.tasks.applyCodeBlockConfig(config) }
            .subscribe { tasks ->
                logger.debug { "Todoist tasks updated, updating view" }
                tasks
                    .map { updateTasks(it, config, store) }
                    .mapLeft { showError(it) }
            }
    }

    fun HTMLElement.updateTasks(viewItems: List<ViewItems>, config: CodeBlockConfig, store: MinionStore) {
        clear()
        if (config.heading.isNotEmpty()) {
            outputHeading(config.heading)
        }

        if (viewItems.isNotEmpty()) {
            append.div {
                viewItems.forEach { viewItem ->
                    outputGroupDiv(viewItem.group, viewItem.items, config, store)
                }
            }
        }
        this.outputItemStats(viewItems)
    }

    fun FlowContent.outputGroupDiv(label: String, items: List<Item>, config: CodeBlockConfig, store: MinionStore) {
        if (label == GROUP_BY_SINGLE) {
            outputItems(items, config, store)
        } else {
            div {
                outputGroupLabel(label, store)
                outputItems(items, config, store)
            }
        }
    }

    fun FlowContent.outputItems(items: List<Item>, config: CodeBlockConfig, store: MinionStore) {
        div {
            items.forEach { task ->
                div(classes = "mi-codeblock-task") {
                    outputItem(task, config, store)
                }
            }
        }
    }

    fun FlowContent.outputItem(item: Item, config: CodeBlockConfig, store: MinionStore) {
        outputCheckbox(item, store)
        span(classes = "mi-codeblock-task-content") {
            item.getPropertyValue(PropertyType.DUE).map { due ->
                outputDue(
                    due,
                    item
                        .getPropertyValue(PropertyType.DUE_IN_PAST)
                        .map { it == "true" }
                        .getOrElse { false }
                )
            }
            span { +" " }
            span {
                outputStyledContent(item.content, store)
            }

            span { +" " }
            item.getPropertyValue(PropertyType.TAGS).map { tags ->
                if (tags.isNotEmpty()) {
                    tags
                        .split(" ")
                        .map { Tag(it) }
                        .toSet()
                        .let {
                            outputTags(it, config)
                        }
                }
            }

            item.getPropertyValue(PropertyType.SOURCE).map {
                outputSource(it, store)
            }
        }
    }
}}
