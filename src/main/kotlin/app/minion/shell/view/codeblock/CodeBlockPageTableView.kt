package app.minion.shell.view.codeblock

import app.minion.core.MinionError
import app.minion.core.model.Content
import app.minion.core.model.DataviewField
import app.minion.core.model.FileData
import app.minion.core.store.MinionStore
import app.minion.shell.view.ViewFunctions.Companion.maybeOutputHeading
import app.minion.shell.view.ViewFunctions.Companion.outputSourceLink
import app.minion.shell.view.ViewFunctions.Companion.outputStyledContent
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.showError
import app.minion.shell.view.codeblock.CodeBlockPageFunctions.Companion.applyCodeBlockConfig
import app.minion.shell.view.codeblock.CodeBlockPageListView.Companion.outputStats
import arrow.core.toOption
import io.kvision.state.sub
import kotlinx.dom.clear
import kotlinx.html.Entities
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
        fileDataMap: Map<String, List<FileData>>,
        element: HTMLElement,
        config: CodeBlockConfig,
        store: MinionStore
    ) {
        element.clear()
        element.maybeOutputHeading(config)

        if (fileDataMap.isNotEmpty()) {
            element.outputTable(fileDataMap, config, store)
        }

        element.outputStats(fileDataMap)
    }

    fun HTMLElement.outputTable(fileDataMap: Map<String, List<FileData>>, config: CodeBlockConfig, store: MinionStore) {
        append.table(classes = "mi-codeblock-table"){
            outputTableHeader(config)
            if (config.groupByOrder.isEmpty()) {
                fileDataMap.forEach { entry ->
                    outputGroup(entry.key, entry.value, config, store)
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
                        outputGroup(entry.key, entry.value, config, store)
                    }
            }
        }
    }

    fun TABLE.outputGroupWithLabel(
        group: String,
        label: String,
        fileDataMap: Map<String, List<FileData>>,
        config: CodeBlockConfig,
        store: MinionStore
    ) {
        fileDataMap[group]
            .toOption().toEither {
                MinionError.GroupByNotFoundError("$group not found in results")
            }
            .map { outputGroup(label, it, config, store) }
            .mapLeft { logger.warn { it } }
    }

    fun TABLE.outputGroup(
        label: String,
        fileDataset: List<FileData>,
        config: CodeBlockConfig,
        store: MinionStore
    ) {
        if (label == GROUP_BY_SINGLE) {
            outputFileData(fileDataset, config, store)
        } else {
            outputGroupHeader(label, config, store)
            outputFileData(fileDataset, config, store)
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

    fun TABLE.outputFileData(fileDataset: List<FileData>, config: CodeBlockConfig, store: MinionStore) {
        fileDataset.forEach { fileData ->
            tr(classes = "mi-codeblock-table-data-row") {
                td(classes = "mi-codeblock-table-data-cell") {
                    outputSourceLink(fileData.name, store)
                }
                config.properties.forEach { field ->
                    fileData.dataview[DataviewField(field)]
                        .toOption()
                        .onSome { td(classes = "mi-codeblock-table-data-cell") { +it.v } }
                        .onNone { td(classes = "mi-codeblock-table-data-cell") {  } }
                }
            }
        }
    }
}}
