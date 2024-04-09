package app.minion.shell.view.codeblock

import app.minion.core.MinionError
import app.minion.core.functions.StatisticsFunctions.Companion.calculateTotalCount
import app.minion.core.model.FileData
import app.minion.core.store.MinionStore
import app.minion.shell.view.ViewFunctions.Companion.maybeOutputHeading
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.outputGroupLabel
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.outputHeading
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.showError
import app.minion.shell.view.codeblock.CodeBlockPageFunctions.Companion.applyCodeBlockConfig
import app.minion.shell.view.codeblock.CodeBlockTaskGalleryView.Companion.outputGroupWithLabel
import app.minion.shell.view.codeblock.components.CodeBlockCard.Companion.outputPageCard
import arrow.core.toOption
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
        fileDataMap: Map<String, List<FileData>>,
        element: HTMLElement,
        store: MinionStore,
        config: CodeBlockConfig
    ) {
        element.clear()
        element.maybeOutputHeading(config)

        if (fileDataMap.isNotEmpty()) {
            element.append.div {
                // if groupByOrder is set, loop over entries pulling out values
                // If order size is less than map size, output remaining entries
                if (config.groupByOrder.isEmpty()) {
                    fileDataMap.forEach { entry ->
                        outputGroupDiv(entry.key, entry.value, config, store)
                    }
                } else {
                    config.groupByOrder.forEach { group ->
                        logger.debug { "Outputting group $group" }
                        if (group.contains(":")) {
                            group.split(":")
                                .let {
                                    outputGroupWithLabel(it[0], it[1], fileDataMap, config, store)
                                }
                        } else {
                            outputGroupWithLabel(group, group, fileDataMap, config, store)
                        }
                    }
                    // Output any remaining entries
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
        fileDataMap: Map<String, List<FileData>>,
        config: CodeBlockConfig,
        store: MinionStore
    ) {
        logger.debug { "outputGroupWithLabel: $group, $label" }
        fileDataMap[group]
            .toOption().toEither {
                MinionError.GroupByNotFoundError("$group not found in results")
            }
            .map { outputGroupDiv(label, it, config, store) }
            .mapLeft {
                // Don't stop processing, just report the issue and continue
                logger.warn { "$it" }
            }
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
        div(classes = "mi-codeblock-page-gallery") {
            fileDataSet.forEach { fileData ->
                outputPageCard(fileData, config, store)
            }
        }
    }

    fun HTMLElement.outputStats(fileDataList: Map<String, List<FileData>>) {
        this.append.div(classes = "mi-codeblock-item-count") {
            +"Page Count: ${fileDataList.calculateTotalCount()}"
        }
    }
}}
