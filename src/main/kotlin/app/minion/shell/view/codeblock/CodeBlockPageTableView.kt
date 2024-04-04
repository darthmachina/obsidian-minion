package app.minion.shell.view.codeblock

import app.minion.core.model.FileData
import app.minion.core.store.MinionStore
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.showError
import app.minion.shell.view.codeblock.CodeBlockPageFunctions.Companion.applyCodeBlockConfig
import io.kvision.state.sub
import kotlinx.dom.clear
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
        fileDataMap: Map<String, List<FileData>>,
        element: HTMLElement,
        config: CodeBlockConfig,
        store: MinionStore
    ) {
        element.clear()
    }
}}