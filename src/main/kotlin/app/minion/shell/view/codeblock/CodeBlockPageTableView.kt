package app.minion.shell.view.codeblock

import app.minion.core.model.Content
import app.minion.core.store.MinionStore
import app.minion.shell.view.Item
import app.minion.shell.view.ViewFunctions.Companion.maybeOutputHeading
import app.minion.shell.view.ViewFunctions.Companion.outputSourceLink
import app.minion.shell.view.ViewFunctions.Companion.outputStyledContent
import app.minion.shell.view.ViewItems
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.showError
import app.minion.shell.view.codeblock.CodeBlockPageFunctions.Companion.applyCodeBlockConfig
import app.minion.shell.view.codeblock.CodeBlockPageListView.Companion.outputStats
import io.kvision.state.sub
import kotlinx.dom.clear
import kotlinx.html.TABLE
import kotlinx.html.dom.append
import kotlinx.html.table
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.tr
import mu.KotlinLogging
import org.w3c.dom.HTMLElement

private val logger = KotlinLogging.logger("CodeBlockPageTableView")

interface CodeBlockPageTableView { companion object {
    fun HTMLElement.addPageTableView(config: CodeBlockConfig, store: MinionStore) {
        classList.add("mi-codeblock")
        store
            .sub { it.error }
            .subscribe { error ->
                error.map { this.showError(it) }
            }
        store
            .sub { it.applyCodeBlockConfig(config) }
            .subscribe { pages ->
                logger.debug { "Page list updated, running updatePages(): $pages" }
                pages
                    .map { updatePages(it, this, config, store) }
                    .mapLeft { this.showError(it) }
            }
    }

    fun updatePages(
        viewItems: List<ViewItems>,
        element: HTMLElement,
        config: CodeBlockConfig,
        store: MinionStore
    ) {
        element.clear()
        element.maybeOutputHeading(config)

        if (viewItems.isNotEmpty()) {
            element.outputTable(viewItems, config, store)
        }

        element.outputStats(viewItems)
    }

    fun HTMLElement.outputTable(viewItems: List<ViewItems>, config: CodeBlockConfig, store: MinionStore) {
        append.table(classes = "mi-codeblock-table"){
            outputTableHeader(config)
            viewItems.forEach { viewItem ->
                outputGroup(viewItem.group, viewItem.items, config, store)
            }
        }
    }

    fun TABLE.outputGroup(
        label: String,
        items: List<Item>,
        config: CodeBlockConfig,
        store: MinionStore
    ) {
        if (label == GROUP_BY_SINGLE) {
            outputItems(items, config, store)
        } else {
            outputGroupHeader(label, config, store)
            outputItems(items, config, store)
        }
    }

    fun TABLE.outputGroupHeader(label: String, config: CodeBlockConfig, store: MinionStore) {
        tr(classes = "mi-codeblock-table-group-header") {
            th(classes = "mi-codeblock-table-header-cell mi-codeblock-table-page-column") {
                colSpan = "${1 + config.properties.size}"
                outputStyledContent(Content(label), store)
            }
        }
    }

    fun TABLE.outputTableHeader(config: CodeBlockConfig) {
        tr(classes = "mi-codeblock-table-header") {
            th(classes = "mi-codeblock-table-header-cell mi-codeblock-table-page-column") { +"Page" }
            config.properties.forEach { property ->
                th(classes = "mi-codeblock-table-header-cell") { +property }
            }
        }
    }

    fun TABLE.outputItems(items: List<Item>, config: CodeBlockConfig, store: MinionStore) {
        items.forEach { item ->
            tr(classes = "mi-codeblock-table-data-row") {
                td(classes = "mi-codeblock-table-data-cell") {
                    item.fileData.map { fileData ->
                        outputSourceLink(fileData.name, store)
                    }.onNone {
                        +item.content.v
                    }
                }
                item.properties.forEach { property ->
                    td(classes = "mi-codeblock-table-data-cell") {
                        outputStyledContent(Content(property.value), store)
                    }
                }
            }
        }
    }
}}
