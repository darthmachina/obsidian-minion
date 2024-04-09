package app.minion.shell.view.codeblock

import app.minion.core.MinionError
import app.minion.core.functions.StatisticsFunctions.Companion.calculateTotalCount
import app.minion.core.model.DataviewField
import app.minion.core.model.FileData
import app.minion.core.store.MinionStore
import app.minion.shell.functions.VaultFunctions
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.outputGroupLabel
import app.minion.shell.view.codeblock.CodeBlockPageFunctions.Companion.applyCodeBlockConfig
import app.minion.shell.view.ICON_HASH
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

    fun updatePages(fileDataMap: Map<String, List<FileData>>, element: HTMLElement, store: MinionStore, config: CodeBlockConfig) {
        element.clear()
        if (config.heading.isNotEmpty()) {
            element.append.div(classes = "mi-codeblock-heading") {
                +config.heading
            }
        }

        if (fileDataMap.isNotEmpty()) {
            element.append.div {
                if (config.groupByOrder.isEmpty()) {
                    fileDataMap.forEach { entry ->
                        outputGroupDiv(entry.key, entry.value, config, store)
                    }
                } else {
                    config.groupByOrder.forEach { group ->
                        if (group.contains(":")) {
                            group.split(":")
                                .let {
                                    outputGroupWithLabel(it[0], it[1], fileDataMap, config, store)
                                }
                        } else {
                            outputGroupWithLabel(group, group, fileDataMap, config, store)
                        }
                    }
                    fileDataMap
                        .filter { entry ->
                            !config.groupByOrder.any { group ->
                                group.startsWith(entry.key)
                            }
                        }
                        .forEach { entry ->
                            outputGroupDiv(entry.key, entry.value, config, store)
                        }
                }
            }
        }

        element.outputStats(fileDataMap)
    }

    fun FlowContent.outputGroupWithLabel(
        group: String,
        label: String,
        fileData: Map<String, List<FileData>>,
        config: CodeBlockConfig,
        store: MinionStore
    ) {
        fileData[group]
            .toOption().toEither {
                MinionError.GroupByNotFoundError("$group not found in results")
            }
            .map { outputGroupDiv(label, it, config, store) }
            .mapLeft { logger.warn { "$it" } }
    }

    fun FlowContent.outputGroupDiv(
        label: String,
        fileDataSet: List<FileData>,
        config: CodeBlockConfig,
        store: MinionStore
    ) {
        if (label == GROUP_BY_SINGLE) {
            outputFileDataSet(config, fileDataSet, store)
        } else {
            div {
                outputGroupLabel(label, store)
                outputFileDataSet(config, fileDataSet, store)
            }
        }
    }

    fun FlowContent.outputFileDataSet(config: CodeBlockConfig, fileDataSet: List<FileData>, store: MinionStore) {
        ul {
            fileDataSet.forEach { fileData ->
                li {
                    span { outputFileData(fileData, store) }
                    if (config.groupBy == GroupByOptions.dataview) {
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

    fun HTMLElement.outputStats(fileDataList: Map<String, List<FileData>>) {
        this.append.div(classes = "mi-codeblock-item-count") {
            +"Page Count: ${fileDataList.calculateTotalCount()}"
        }
    }
}}
