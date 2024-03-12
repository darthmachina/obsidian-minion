package app.minion.shell.view.codeblock

import app.minion.core.MinionError
import app.minion.core.functions.StatisticsFunctions.Companion.calculateTotalCount
import app.minion.core.model.DataviewField
import app.minion.core.model.FileData
import app.minion.core.store.MinionStore
import app.minion.shell.functions.VaultFunctions
import app.minion.shell.view.codeblock.CodeBlockPageFunctions.Companion.applyCodeBlockConfig
import app.minion.shell.view.iconHash
import app.minion.shell.view.modal.UpdateDataviewValue
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

    fun updatePages(fileDataMap: Map<String, Set<FileData>>, element: HTMLElement, store: MinionStore, config: CodeBlockConfig) {
        element.clear()
        if (config.heading.isNotEmpty()) {
            element.append.div(classes = "mi-codeblock-heading") {
                +config.heading
            }
        }

        if (fileDataMap.isNotEmpty()) {
            element.append.div {
                fileDataMap.forEach { entry ->
                    outputGroup(config, entry.key, entry.value, store)
                }
            }
        }

        element.outputStats(fileDataMap)
    }

    fun FlowContent.outputGroup(config: CodeBlockConfig, label: String, fileDataSet: Set<FileData>, store: MinionStore) {
        if (label == GROUP_BY_SINGLE) {
            outputFileDataSet(config, fileDataSet, store)
        } else {
            ul {
                li {
                    +label
                    outputFileDataSet(config, fileDataSet, store)
                }
            }
        }
    }

    fun FlowContent.outputFileDataSet(config: CodeBlockConfig, fileDataSet: Set<FileData>, store: MinionStore) {
        ul {
            fileDataSet.forEach { fileData ->
                li {
                    span { outputFileData(fileData, store) }
                    if (config.groupBy == GroupByOptions.dataview) {
                        span(classes = "mi-icon mi-button") {
                            title = "Change group"
                            unsafe { +iconHash }
                            onClickFunction = {
                                UpdateDataviewValue(
                                    fileData,
                                    config.groupByField,
                                    fileData.dataview[DataviewField(config.groupByField)]!!,
                                    store.store.state.dataviewValueCache[DataviewField(config.groupByField)]!!,
                                    store,
                                    store.store.state.plugin.app
                                ).open()
                            }
                        }
                    }
                }
            }
        }
    }

    fun FlowContent.outputFileData(fileData: FileData, store: MinionStore) {
        span(classes = "mi-codeblock-source-link") {
            +fileData.name.v
            onClickFunction = {
                VaultFunctions.openSourceFile(fileData.name, store.store.state.plugin.app)
            }
        }
    }

    fun HTMLElement.outputStats(fileDataList: Map<String, Set<FileData>>) {
        this.append.div(classes = "mi-codeblock-item-count") {
            +"Page Count: ${fileDataList.calculateTotalCount()}"
        }
    }
}}
