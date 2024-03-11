package app.minion.shell.view.codeblock

import app.minion.core.MinionError
import app.minion.core.model.FileData
import app.minion.core.store.MinionStore
import app.minion.shell.view.codeblock.CodeBlockPageFunctions.Companion.applyCodeBlockConfig
import io.kvision.state.sub
import kotlinx.dom.clear
import kotlinx.html.div
import kotlinx.html.dom.append
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

    fun updatePages(fileDataList: List<FileData>, element: HTMLElement, store: MinionStore, config: CodeBlockConfig) {

    }

    fun List<FileData>.applyCodeBlockConfig(config: CodeBlockConfig) : List<FileData> {
        return this
    }
}}