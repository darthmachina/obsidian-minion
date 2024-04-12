package app.minion.shell.view.codeblock

import app.minion.core.functions.StatisticsFunctions.Companion.calculateTotalCount
import app.minion.core.store.MinionStore
import app.minion.shell.view.Item
import app.minion.shell.view.ViewFunctions.Companion.maybeOutputHeading
import app.minion.shell.view.ViewItems
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.outputGroupLabel
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.showError
import app.minion.shell.view.codeblock.CodeBlockPageFunctions.Companion.applyCodeBlockConfig
import app.minion.shell.view.codeblock.components.CodeBlockCard.Companion.outputPageCard
import io.kvision.state.sub
import kotlinx.dom.clear
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.dom.append
import mu.KotlinLogging
import org.w3c.dom.HTMLElement

private val logger = KotlinLogging.logger("CodeBlockPageGalleryView")

interface CodeBlockPageGalleryView { companion object {
    fun HTMLElement.addPageGalleryView(config: CodeBlockConfig, store: MinionStore) {
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
                    .map { updatePages(it, this, store, config) }
                    .mapLeft { this.showError(it) }
            }
    }

    fun updatePages(
        viewItems: List<ViewItems>,
        element: HTMLElement,
        store: MinionStore,
        config: CodeBlockConfig
    ) {
        element.clear()
        element.maybeOutputHeading(config)

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
        items: List<Item>,
        config: CodeBlockConfig,
        store: MinionStore
    ) {
        if (label == GROUP_BY_SINGLE) {
            outputItems(config, items, store)
        } else {
            div {
                outputGroupLabel(label, store)
                outputItems(config, items, store)
            }
        }
    }

    fun FlowContent.outputItems(config: CodeBlockConfig, items: List<Item>, store: MinionStore) {
        div(classes = "mi-codeblock-page-gallery") {
            items.forEach { item ->
                outputPageCard(item, config, store)
            }
        }
    }

    fun HTMLElement.outputStats(viewItems: List<ViewItems>) {
        this.append.div(classes = "mi-codeblock-item-count") {
            +"Page Count: ${viewItems.calculateTotalCount()}"
        }
    }
}}
