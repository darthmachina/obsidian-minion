package app.minion.shell.view.codeblock.components

import app.minion.core.functions.DateTimeFunctions.Companion.asString
import app.minion.core.functions.TaskTagFunctions.Companion.asString
import app.minion.core.functions.TaskTagFunctions.Companion.collectTags
import app.minion.core.model.Content
import app.minion.core.model.DataviewField
import app.minion.core.model.FileData
import app.minion.core.model.Task
import app.minion.core.store.MinionStore
import app.minion.shell.functions.VaultFunctions
import app.minion.shell.thunk.TaskThunks
import app.minion.shell.view.ViewFunctions.Companion.outputDue
import app.minion.shell.view.ViewFunctions.Companion.outputStyledContent
import app.minion.shell.view.codeblock.CodeBlockConfig
import app.minion.shell.view.codeblock.CodeBlockDisplay
import app.minion.shell.view.codeblock.CodeBlockOptions
import app.minion.shell.view.codeblock.CodeBlockTaskFunctions.Companion.removeConfigTags
import app.minion.shell.view.codeblock.PROPERTY_DUE
import app.minion.shell.view.codeblock.PROPERTY_SOURCE
import app.minion.shell.view.codeblock.PROPERTY_TAGS
import app.minion.shell.view.codeblock.components.CodeBlockCardFunctions.Companion.createChangeGroupMenuItem
import app.minion.shell.view.codeblock.components.CodeBlockCardFunctions.Companion.createChangeKanbanMenuItem
import app.minion.shell.view.codeblock.components.CodeBlockCardFunctions.Companion.getImagePath
import app.minion.shell.view.codeblock.components.CodeBlockCardFunctions.Companion.outputCardMenu
import app.minion.shell.view.codeblock.components.CodeBlockCardFunctions.Companion.outputSubtasks
import app.minion.shell.view.iconRepeat
import arrow.core.None
import arrow.core.Option
import arrow.core.toOption
import kotlinx.html.FlowContent
import kotlinx.html.checkBoxInput
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
        fileData: FileData,
        config: CodeBlockConfig,
        store: MinionStore
    ) {
        val image = fileData
            .getImagePath(store)
            .map { it }
            .mapLeft { logger.warn { it } }
            .getOrNone()
        val properties = config
            .properties
            .mapNotNull { field ->
                fileData.dataview[DataviewField(field)]
                    .toOption()
                    .map { field to { span { +it.v } } }
                    .getOrNull()
            }

        outputCard(
            {
                span(classes = "mi-codeblock-source-link") {
                +fileData.name.v
                onClickFunction = {
                    VaultFunctions.openSourceFile(fileData.name, store.store.state.plugin.app)
                }
            }},
            {},
            properties,
            image,
            listOf(createChangeGroupMenuItem(fileData, config, store)),
            config,
        )
    }

    fun FlowContent.outputTaskCard(task: Task, config: CodeBlockConfig, store: MinionStore) {
        val properties = config
            .properties
            .mapNotNull { field ->
                when(field) {
                    PROPERTY_DUE -> {
                        task.dueDate.map {due ->
                            "Due" to {
                                span {
                                    outputDue(due)
                                    task.repeatInfo.map {
                                        span(classes = "mi-icon") {
                                            title = it.asString()
                                            unsafe { +iconRepeat }
                                        }
                                    }
                                }
                            }
                        }.getOrNull()
                    }
                    PROPERTY_TAGS -> {
                        "Tags" to {
                            span {
                                +task.collectTags().removeConfigTags(config).asString()
                            }
                        }
                    }
                    PROPERTY_SOURCE -> {
                        "Source" to {
                            span {
                                outputStyledContent(Content("[[${task.fileInfo.file.v}]]"), store)
                            }

                        }
                    }
                    else -> {
                        logger.debug { "Property $field not supported yet" }
                        null
                    }
                }
            }

        outputCard(
            {
                checkBoxInput {
                    onClickFunction = {
                        store.dispatch(TaskThunks.completeTask(store.store.state.plugin.app, task))
                    }
                }
                outputStyledContent(task.content, store)
            },
            { outputSubtasks(task, store) },
            properties,
            None,
            listOf(createChangeKanbanMenuItem(task, config, store)),
            config,
        )
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
