package app.minion.shell.view.codeblock.components

import app.minion.shell.view.codeblock.CodeBlockConfig
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.img

interface CodeBlockCard { companion object {
    /**
     * @param title Function to produce the card title
     * @param description Function to produce the card subtitles (e.g. subtask list)
     * @param properties List of key/value Pairs for data to included at the bottom
     * @param config Configuration settings from code block
     */
    fun FlowContent.outputPageCard(
        title: () -> FlowContent,
        description: () -> FlowContent,
        properties: List<Pair<String, String>>,
        config: CodeBlockConfig
    ) {
        div { // Main card
            // Image
            div(classes = "mi-codeblock-cover-image-container") {
                div(classes = "mi-codeblock-cover-image") {}
            }
            // Title
            div(classes = "mi-codeblock-card-title") {
                title()
            }
            // Description
            div(classes = "mi-codeblock-card-description") {
                description()
            }
            // Properties
            div(classes = "mi-codeblock-card-properties") {
                properties.forEach {
                    div(classes = "mi-codeblock-card-properties-label") { it.first }
                    div(classes = "mi-codeblock-card-properties-value") { it.second }
                }
            }
        }
    }

}}