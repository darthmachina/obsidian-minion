package app.minion.shell.view.codeblock

import app.minion.core.MinionError
import app.minion.core.functions.StatisticsFunctions.Companion.calculateTotalCount
import app.minion.core.model.Content
import app.minion.core.model.DataviewField
import app.minion.core.model.FileData
import app.minion.core.store.MinionStore
import app.minion.shell.functions.VaultFunctions
import app.minion.shell.view.ViewFunctions.Companion.outputStyledContent
import app.minion.shell.view.codeblock.CodeBlockPageFunctions.Companion.applyCodeBlockConfig
import app.minion.shell.view.codeblock.CodeBlockPageListView.Companion.addPageListView
import app.minion.shell.view.codeblock.CodeBlockPageListView.Companion.outputFileDataSet
import app.minion.shell.view.codeblock.CodeBlockPageListView.Companion.outputGroup
import app.minion.shell.view.codeblock.CodeBlockPageListView.Companion.outputStats
import app.minion.shell.view.iconHash
import app.minion.shell.view.modal.UpdateDataviewValue
import arrow.core.None
import arrow.core.Some
import arrow.core.toOption
import io.kvision.state.sub
import kotlinx.dom.clear
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.js.onClickFunction
import kotlinx.html.label
import kotlinx.html.li
import kotlinx.html.span
import kotlinx.html.title
import kotlinx.html.ul
import kotlinx.html.unsafe
import mu.KotlinLogging
import org.w3c.dom.HTMLElement

private val logger = KotlinLogging.logger("CodeBlockPageGalleryView")

interface CodeBlockPageGalleryView { companion object {
    fun HTMLElement.addPageGalleryView(config: CodeBlockConfig, store: MinionStore) {
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
                // if groupByOrder is set, loop over entries pulling out values
                // If order size is less than map size, output remaining entries
                if (config.groupByOrder.isEmpty()) {
                    fileDataMap.forEach { entry ->
                        outputGroup(config, entry.key, entry.value, store)
                    }
                } else {
                    config.groupByOrder.forEach { group ->
                        fileDataMap[group]
                            .toOption().toEither {
                                MinionError.GroupByNotFoundError("$group not found in results")
                            }
                            .map { outputGroup(config, group, it, store) }
                            .mapLeft {
                                // Don't stop processing, just report the issue and continue
                                logger.warn { "$it" }
                            }
                    }
                    // Output any remaining entries
                    fileDataMap
                        .filter { !config.groupByOrder.contains(it.key) }
                        .forEach { entry ->
                            outputGroup(config, entry.key, entry.value, store)
                        }
                }
            }
        }

        element.outputStats(fileDataMap)
    }

    fun FlowContent.outputGroup(config: CodeBlockConfig, label: String, fileDataSet: Set<FileData>, store: MinionStore) {
        if (label == GROUP_BY_SINGLE) {
            outputFileDataSet(config, fileDataSet, store)
        } else {
            div {
                div(classes = "mi-codeblock-group-label") { +label }
                outputFileDataSet(config, fileDataSet, store)
            }
        }
    }

    fun FlowContent.outputFileDataSet(config: CodeBlockConfig, fileDataSet: Set<FileData>, store: MinionStore) {
        div(classes = "mi-codeblock-page-gallery") {
            fileDataSet.forEach { fileData ->
                outputFileData(fileData, config, store)
            }
        }
    }

    fun FlowContent.outputFileData(fileData: FileData, config: CodeBlockConfig, store: MinionStore) {
        div {
            div(classes = "mi-codeblock-page-gallery-title") {
                span(classes = "mi-codeblock-source-link") {
                    +fileData.name.v
                    onClickFunction = {
                        VaultFunctions.openSourceFile(fileData.name, store.store.state.plugin.app)
                    }
                }
                span(classes = "mi-icon mi-button") {
                    title = "Change group value"
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
            if (config.properties.isNotEmpty()) {
                outputProperties(fileData, config, store)
            }
        }
    }

    fun FlowContent.outputProperties(fileData: FileData, config: CodeBlockConfig, store: MinionStore) {
        div(classes = "mi-codeblock-page-gallery-fields") {
            config.properties.forEach { property ->
                outputProperty(property, fileData, store)
            }
        }
    }

    fun FlowContent.outputProperty(label: String, fileData: FileData, store: MinionStore) {
        when(label) {
            PROPERTY_CREATED -> {}
            PROPERTY_MODIFIED -> {}
            PROPERTY_SOURCE -> {}
            PROPERTY_DUE -> {}
            PROPERTY_TAGS -> {}
            else -> {
                // Is a dataview field
                when(val value = fileData.dataview[DataviewField(label)].toOption()) {
                    is Some -> {
                        div(classes = "mi-codeblock-page-gallery-fields-label") {
                            +label
                        }
                        div(classes = "mi-codeblock-page-gallery-fields-value") {
                            outputStyledContent(Content(value.value.v), store)
                        }
                    }
                    is None -> {}
                }
            }
        }
    }

    fun HTMLElement.outputStats(fileDataList: Map<String, Set<FileData>>) {
        this.append.div(classes = "mi-codeblock-item-count") {
            +"Page Count: ${fileDataList.calculateTotalCount()}"
        }
    }
}}
