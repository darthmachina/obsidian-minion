package app.minion.shell.view.codeblock

import app.minion.core.MinionError
import app.minion.core.functions.StatisticsFunctions.Companion.calculateTotalCount
import app.minion.core.model.DataviewField
import app.minion.core.store.MinionStore
import app.minion.shell.functions.VaultFunctions
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.outputGroupLabel
import app.minion.shell.view.codeblock.CodeBlockPageFunctions.Companion.applyCodeBlockConfig
import app.minion.shell.view.ICON_HASH
import app.minion.shell.view.Item
import app.minion.shell.view.ViewItems
import app.minion.shell.view.modal.UpdateDataviewValue
import arrow.core.toOption
import io.kvision.state.sub
import kotlinx.dom.clear
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.js.onClickFunction
import kotlinx.html.li
import kotlinx.html.span
import kotlinx.html.title
import kotlinx.html.ul
import kotlinx.html.unsafe
import mu.KotlinLogging
import org.w3c.dom.HTMLElement

private val logger = KotlinLogging.logger("CodeBlockPageListView")

interface CodeBlockPageListView { companion object {
    fun HTMLElement.addPageListView(config: CodeBlockConfig, store: MinionStore) {
        classList.add("mi-codeblock")
        store
            .sub { it.error }
            .subscribe { error ->
                error.map { showError(it, this) }
            }
        store
            .sub { it.applyCodeBlockConfig(config) }
            .subscribe { pages ->
                logger.debug { "Page list updated, running updatePages(): $pages" }
                pages
                    .map { updatePages(it, this, store, config) }
                    .mapLeft { showError(it, this) }
            }
    }

    fun showError(error: MinionError, element: HTMLElement) {
        element.clear()
        element.append.div { +error.message }
    }

    fun updatePages(viewItems: List<ViewItems>, element: HTMLElement, store: MinionStore, config: CodeBlockConfig) {
        element.clear()
        if (config.heading.isNotEmpty()) {
            element.append.div(classes = "mi-codeblock-heading") {
                +config.heading
            }
        }

        if (viewItems.isNotEmpty()) {
            element.append.div {
                viewItems.forEach { viewItem ->
                    outputGroupDiv(viewItem.group, viewItem.items, config, store)
                }
            }
        }

        element.outputStats(viewItems)
    }

    fun FlowContent.outputGroupDiv(
        label: String,
        fileDataSet: List<Item>,
        config: CodeBlockConfig,
        store: MinionStore
    ) {
        if (label == GROUP_BY_SINGLE) {
            outputItems(config, fileDataSet, store)
        } else {
            div {
                outputGroupLabel(label, store)
                outputItems(config, fileDataSet, store)
            }
        }
    }

    fun FlowContent.outputItems(config: CodeBlockConfig, items: List<Item>, store: MinionStore) {
        ul {
            items.forEach { item ->
                li {
                    span { outputItem(item, store) }
                    if (config.groupBy == GroupByOptions.dataview) {
                        item.fileData.onSome { fileData ->
                            span(classes = "mi-icon mi-button") {
                                title = "Change group"
                                unsafe { +ICON_HASH }
                                onClickFunction = {
                                    UpdateDataviewValue(
                                        fileData,
                                        config.groupByField,
                                        fileData.dataview[DataviewField(config.groupByField)].toOption(),
                                        store.store.state.dataviewValueCache[DataviewField(config.groupByField)]!!,
                                        store,
                                        store.store.state.plugin.app
                                    ).open()
                                }
                            }
                        }.onNone {
                            logger.error { "${item.content.v} has no fileData value" }
                        }
                    }
                }
            }
        }
    }

    fun FlowContent.outputItem(item: Item, store: MinionStore) {
        span(classes = "mi-codeblock-source-link") {
            +item.content.v
            item.fileData.map {fileData ->
                onClickFunction = {
                    VaultFunctions.openSourceFile(fileData.name, store.store.state.plugin.app)
                }
            }.onNone {
                logger.error { "${item.content.v} has no fileData value" }
            }
        }
    }

    fun HTMLElement.outputStats(viewItems: List<ViewItems>) {
        this.append.div(classes = "mi-codeblock-item-count") {
            +"Page Count: ${viewItems.calculateTotalCount()}"
        }
    }
}}
