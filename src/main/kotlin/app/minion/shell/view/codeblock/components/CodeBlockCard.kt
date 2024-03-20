package app.minion.shell.view.codeblock.components

import app.minion.core.model.FileData
import app.minion.core.store.MinionStore
import app.minion.shell.functions.VaultFunctions
import app.minion.shell.view.codeblock.CodeBlockConfig
import app.minion.shell.view.codeblock.CodeBlockDisplay
import app.minion.shell.view.codeblock.CodeBlockOptions
import app.minion.shell.view.codeblock.GroupByOptions
import app.minion.shell.view.codeblock.components.CodeBlockCardFunctions.Companion.createChangeGroupMenuItem
import app.minion.shell.view.codeblock.components.CodeBlockCardFunctions.Companion.getImagePath
import app.minion.shell.view.codeblock.components.CodeBlockCardFunctions.Companion.outputCardMenu
import arrow.core.Option
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.img
import kotlinx.html.js.onClickFunction
import kotlinx.html.span
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

        outputCard(
            { span(classes = "mi-codeblock-source-link") {
                +fileData.name.v
                onClickFunction = {
                    VaultFunctions.openSourceFile(fileData.name, store.store.state.plugin.app)
                }
            }},
            { span { +"Description" } },
            emptyList(),
            image,
            listOf(createChangeGroupMenuItem(fileData, config, store)),
            config
        )
    }

    fun FlowContent.outputTaskCard() {

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
        properties: List<Pair<String, String>>,
        image: Option<String>,
        menuItems: List<FlowContent.() -> Unit>,
        config: CodeBlockConfig
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
                    } else -> {} // Other display settings aren't involved here
                }
            }
            // Title
            div(classes = "mi-codeblock-card-title-container") {
                span(classes = "mi-codeblock-card-title") { title() }
                outputCardMenu(menuItems)
                if (config.groupBy != GroupByOptions.NONE) {
                    outputCardMenu(menuItems)
                }
            }
            // Description
            div(classes = "mi-codeblock-card-description") {
                description()
            }
            // Properties
            if (properties.isNotEmpty()) {
                div(classes = "mi-codeblock-card-properties") {
                    properties.forEach {
                        div(classes = "mi-codeblock-card-properties-label") { it.first }
                        div(classes = "mi-codeblock-card-properties-value") { it.second }
                    }
                }
            }
        }
    }

}}
