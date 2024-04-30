package app.minion.shell.view.codeblock.components

import app.minion.core.functions.TaskTagFunctions.Companion.asString
import app.minion.core.functions.TaskTagFunctions.Companion.collectTags
import app.minion.core.model.Content
import app.minion.core.model.DataviewField
import app.minion.core.model.FileData
import app.minion.core.model.Task
import app.minion.core.store.MinionStore
import app.minion.shell.functions.VaultFunctions
import app.minion.shell.view.ViewFunctions.Companion.outputCheckbox
import app.minion.shell.view.ViewFunctions.Companion.outputDue
import app.minion.shell.view.ViewFunctions.Companion.outputStyledContent
import app.minion.shell.view.codeblock.CodeBlockConfig
import app.minion.shell.view.codeblock.CodeBlockDisplay
import app.minion.shell.view.codeblock.CodeBlockOptions
import app.minion.shell.view.codeblock.CodeBlockTaskFunctions.Companion.removeConfigTags
import app.minion.shell.view.codeblock.PROPERTY_DUE
import app.minion.shell.view.codeblock.PROPERTY_EISENHOWER
import app.minion.shell.view.codeblock.PROPERTY_SOURCE
import app.minion.shell.view.codeblock.PROPERTY_TAGS
import app.minion.shell.view.codeblock.components.CodeBlockCardFunctions.Companion.createChangeGroupMenuItem
import app.minion.shell.view.codeblock.components.CodeBlockCardFunctions.Companion.createChangeKanbanMenuItem
import app.minion.shell.view.codeblock.components.CodeBlockCardFunctions.Companion.getImagePath
import app.minion.shell.view.codeblock.components.CodeBlockCardFunctions.Companion.outputCardMenu
import app.minion.shell.view.codeblock.components.CodeBlockCardFunctions.Companion.outputSubtasks
import app.minion.shell.view.ICON_IMPORTANT
import app.minion.shell.view.ICON_REPEAT
import app.minion.shell.view.ICON_URGENT
import app.minion.shell.view.Item
import app.minion.shell.view.PropertyType
import app.minion.shell.view.ViewFunctions.Companion.outputIcon
import app.minion.shell.view.ViewModelFunctions.Companion.getPropertyValue
import arrow.core.None
import arrow.core.Option
import arrow.core.flatMap
import arrow.core.getOrElse
import arrow.core.toOption
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.img
import kotlinx.html.js.onClickFunction
import kotlinx.html.span
import kotlinx.html.title
import kotlinx.html.unsafe
import mu.KotlinLogging

private val logger = KotlinLogging.logger("CodeBlockCard")

interface CodeBlockCard { companion object {
    fun FlowContent.outputPageCard(
        item: Item,
        config: CodeBlockConfig,
        store: MinionStore
    ) {
        val image = item
            .getImagePath(store)
            .mapLeft { logger.warn { it } }
            .getOrNone()
        val properties = item.properties.mapNotNull { property ->
            when (property.type) {
                PropertyType.DATAVIEW, PropertyType.FORMULA -> {
                    property.name to { span {
                        outputStyledContent(Content(property.value), store)
                    } }
                }
                PropertyType.DUE -> TODO("PropertyType.DUE")
                PropertyType.TAGS -> TODO("PropertyType.TAGS")
                PropertyType.SOURCE -> TODO("PropertyType.SOURCE")
                PropertyType.SECTION -> TODO("PropertyType.SECTION")
                PropertyType.IMAGE -> null // Processed separately
                PropertyType.DUE_IN_PAST -> TODO("PropertyType.DUE_IN_PAST")
                PropertyType.LIFE_AREA_COLOR -> TODO("PropertyType.LIFE_AREA_COLOR")
                PropertyType.EISENHOWER -> TODO("PropertyType.EISENHOWER")
                PropertyType.REPEAT_INFO -> TODO("PropertyType.REPEAT_INFO")
                PropertyType.COMPLETED_SUBTASK_PERCENT -> TODO("PropertyType.COMPLETE_SUBTASK_PERCENT")
            }
        }

        item.fileData.map {fileData ->
            outputCard(
                {
                    span(classes = "mi-codeblock-source-link") {
                        +item.content.v
                        onClickFunction = {
                            VaultFunctions.openSourceFile(fileData.name, store.store.state.plugin.app)
                        }
                    }
                },
                {},
                properties,
                image,
                listOf(createChangeGroupMenuItem(fileData, config, store)),
                config,
            )
        }
    }

    fun FlowContent.outputTaskCard(item: Item, config: CodeBlockConfig, store: MinionStore) {
        val properties = item.properties.mapNotNull { property ->
            when (property.type) {
                PropertyType.DUE -> {
                    property.name to {
                        span {
                            outputDue(
                                property.value,
                                item.getPropertyValue(PropertyType.DUE_IN_PAST).map { it == "true" }.getOrElse { false }
                            )
                            item.getPropertyValue(PropertyType.REPEAT_INFO).map {
                                outputIcon(ICON_REPEAT, it)
                            }
                        }
                    }
                }
                PropertyType.TAGS -> {
                    property.name to {
                        span {
                            +property.value
                        }
                    }
                }
                PropertyType.SOURCE -> {
                    property.name to {
                        span {
                            outputStyledContent(Content(property.value), store)
                        }
                    }
                }
                PropertyType.SECTION -> {
                    property.name to {
                        span {
                            +property.value
                        }
                    }
                }
                PropertyType.DATAVIEW -> TODO()
                PropertyType.IMAGE -> TODO()
                PropertyType.FORMULA -> TODO()
                PropertyType.LIFE_AREA_COLOR -> TODO()
                PropertyType.EISENHOWER -> {
                    "Eisenhower" to {
                        span {
                            if (property.value.contains("i")) {
                                outputIcon(ICON_IMPORTANT, "Important")
                            }
                            if (property.value.contains("u")) {
                                outputIcon(ICON_URGENT, "Urgent")
                            }
                        }
                    }
                }
                PropertyType.COMPLETED_SUBTASK_PERCENT -> TODO()
                PropertyType.REPEAT_INFO -> null // Included in other Property output
                PropertyType.DUE_IN_PAST -> null // Included in other Property output
            }
        }

        item.task.map { task ->
            outputCard(
                {
                    outputCheckbox(item, store)
                    outputStyledContent(item.content, store)
                },
                { outputSubtasks(item, store) },
                properties,
                None,
                listOf(createChangeKanbanMenuItem(task, config, store)),
                config,
            )
        }.onNone {
            logger.error { "No task set for item ${item.content.v}" }
        }
    }

    /**
     * @param title Function to produce the card title
     * @param description Function to produce the card subtitles (e.g. subtask list)
     * @param properties List of key/value Pairs for data to included at the bottom
     * @param image Optional link to an image file for the cover
     * @param config Configuration settings from code block
     */
    fun FlowContent.outputCard(
        title: FlowContent.() -> Unit,
        description: FlowContent.() -> Unit,
        properties: List<Pair<String, () -> Unit>>,
        image: Option<String>,
        menuItems: List<FlowContent.() -> Unit>,
        config: CodeBlockConfig,
    ) {
        div(classes = "mi-codeblock-card") { // Main card
            // Image
            if (config.options.contains(CodeBlockOptions.image_on_cover)) {
                when(config.display) {
                    CodeBlockDisplay.gallery -> {
                        div(classes = "mi-codeblock-cover-image-container") {
                            image.map {
                                img(classes = "mi-codeblock-cover-image", src = it)
                            }
                        }
                    }
                    CodeBlockDisplay.kanban -> {
                        image.map {
                            div(classes = "mi-codeblock-cover-image-container") {
                                img(classes = "mi-codeblock-cover-image", src = it)
                            }
                        }
                    } else -> {
                        // Other display settings aren't involved here
                        logger.info { "No image display for ${config.display}" }
                    }
                }
            }
            // Title
            div(classes = "mi-codeblock-card-title-container") {
                span(classes = "mi-codeblock-card-title") { title() }
                outputCardMenu(menuItems)
            }
            // Description
            div(classes = "mi-codeblock-card-description") {
                description()
            }
            // Properties
            if (properties.isNotEmpty()) {
                div(classes = "mi-codeblock-card-properties") {
                    properties.forEach {
                        div(classes = "mi-codeblock-card-properties-label") { +it.first }
                        div(classes = "mi-codeblock-card-properties-value") {
                            it.second()
                        }
                    }
                }
            }
        }
    }
}}
