package app.minion.shell.view.codeblock

import app.minion.core.MinionError
import app.minion.core.functions.StatisticsFunctions.Companion.calculateTotalCount
import app.minion.core.model.Content
import app.minion.core.model.DataviewField
import app.minion.core.model.FileData
import app.minion.core.store.MinionStore
import app.minion.shell.functions.VaultFunctions
import app.minion.shell.view.ViewFunctions.Companion.getWikilinkResourcePath
import app.minion.shell.view.ViewFunctions.Companion.outputStyledContent
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.showError
import app.minion.shell.view.codeblock.CodeBlockPageFunctions.Companion.applyCodeBlockConfig
import app.minion.shell.view.codeblock.CodeBlockPageGalleryView.Companion.addPageGalleryView
import app.minion.shell.view.iconGroup
import app.minion.shell.view.iconHash
import app.minion.shell.view.iconMenu
import app.minion.shell.view.modal.UpdateDataviewValue
import arrow.core.*
import io.kvision.state.sub
import kotlinx.dom.clear
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.js.onClickFunction
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
        fileDataMap: Map<String, Set<FileData>>,
        element: HTMLElement,
        store: MinionStore,
        config: CodeBlockConfig
    ) {
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

    fun FlowContent.outputGroup(
        config: CodeBlockConfig,
        label: String,
        fileDataSet: Set<FileData>,
        store: MinionStore
    ) {
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
            val includeImage = config.options.contains(CodeBlockOptions.image_on_cover)
            var imageIncluded = false
            if (includeImage) {
                fileData.dataview[DataviewField(FIELD_IMAGE)]
                    .toOption()
                    .toEither { MinionError.ImageNotFoundError("No image specified for ${fileData.name.v}") }
                    .flatMap {
                        it.v.getWikilinkResourcePath(
                            store.store.state.plugin.app.vault,
                            store.store.state.plugin.app.metadataCache
                        )
                    }
                    .map { imagePath ->
                        imageIncluded = true
                        div(classes = "mi-codeblock-cover-image-container") {
                            img(classes = "mi-codeblock-cover-image", src = imagePath)
                        }
                    }
                    .mapLeft {
                        logger.warn { "$it" }
                    }
                }
            div(classes = "mi-codeblock-page-gallery-title${if (!imageIncluded) " mi-full-height" else ""}") {
                span(classes = "mi-codeblock-source-link") {
                    +fileData.name.v
                    onClickFunction = {
                        VaultFunctions.openSourceFile(fileData.name, store.store.state.plugin.app)
                    }
                }
                div(classes = "mi-codeblock-menu-container") {
                    span(classes = "mi-icon mi-button") {
                        unsafe { +iconMenu }
                    }
                    div(classes = "mi-codeblock-menu") {
                        a {
                            title = "Change group value"
                            unsafe { +iconGroup }
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
            if (config.properties.isNotEmpty()) {
                outputFields(fileData, config, store)
            }
        }
    }

    fun FlowContent.outputFields(fileData: FileData, config: CodeBlockConfig, store: MinionStore) {
        div(classes = "mi-codeblock-page-gallery-fields") {
            config.properties.forEach { property ->
                outputField(property, fileData, store)
            }
        }
    }

    fun FlowContent.outputField(label: String, fileData: FileData, store: MinionStore) {
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
