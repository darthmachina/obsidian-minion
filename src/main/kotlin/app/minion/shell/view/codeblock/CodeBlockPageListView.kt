package app.minion.shell.view.codeblock

import app.minion.core.MinionError
import app.minion.core.model.FileData
import app.minion.core.store.MinionStore
import app.minion.shell.functions.VaultFunctions
import app.minion.shell.view.codeblock.CodeBlockPageFunctions.Companion.applyCodeBlockConfig
import io.kvision.state.sub
import kotlinx.dom.clear
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.js.onClickFunction
import kotlinx.html.li
import kotlinx.html.span
import kotlinx.html.ul
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
        element.clear()
        if (config.heading.isNotEmpty()) {
            element.append.div(classes = "mi-codeblock-heading") {
                +config.heading
            }
        }

        if (fileDataList.isNotEmpty()) {
            element.append.ul {
                fileDataList.forEach { fileData ->
                    li {
                        outputFileData(fileData, store)
                    }
                }
            }
        }

        element.outputStats(fileDataList)
    }

    fun FlowContent.outputFileData(fileData: FileData, store: MinionStore) {
        span(classes = "mi-codeblock-source-link") {
            +fileData.name.v
            onClickFunction = {
                VaultFunctions.openSourceFile(fileData.name, store.store.state.plugin.app)
            }
        }
    }

    fun HTMLElement.outputStats(fileDataList: List<FileData>) {
        this.append.div(classes = "mi-codeblock-item-count") {
            +"Page Count: ${fileDataList.size}"
        }
    }
}}
