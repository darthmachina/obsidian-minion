package app.minion.shell.view

import MarkdownPostProcessorContext
import app.minion.core.MinionError
import app.minion.core.store.MinionStore
import app.minion.shell.view.codeblock.CodeBlockCardTestView.Companion.addCardTestView
import app.minion.shell.view.codeblock.CodeBlockConfig
import app.minion.shell.view.codeblock.CodeBlockDisplay
import app.minion.shell.view.codeblock.CodeBlockErrorView.Companion.addErrorView
import app.minion.shell.view.codeblock.CodeBlockPageGalleryView.Companion.addPageGalleryView
import app.minion.shell.view.codeblock.CodeBlockPageListView.Companion.addPageListView
import app.minion.shell.view.codeblock.CodeBlockQuery
import app.minion.shell.view.codeblock.CodeBlockTaskGalleryView.Companion.addTaskGalleryView
import app.minion.shell.view.codeblock.CodeBlockTaskListView.Companion.addTaskListView
import arrow.core.toOption
import net.mamoe.yamlkt.Yaml
import org.w3c.dom.HTMLElement

interface CodeBlockView { companion object {
    fun processCodeBlock(store: MinionStore)
    : (source: String, element: HTMLElement, context: MarkdownPostProcessorContext) -> Unit {
        return { source, element, _ ->
            runCatching {
                val config = Yaml.decodeFromString(CodeBlockConfig.serializer(), source)

                when(config.query) {
                    CodeBlockQuery.pages -> processPageCodeBlock(config, store, element)
                    CodeBlockQuery.tasks -> processTaskCodeBlock(config, store, element)
                }
            }
                .onFailure {
                    MinionError.ConfigParseError("Error parsing config", it.toOption())
                        .let {
                            element.addErrorView(it)
                        }
                }
        }
    }

    fun processTaskCodeBlock(config: CodeBlockConfig, store: MinionStore, element: HTMLElement) {
        when(config.display) {
            CodeBlockDisplay.list -> element.addTaskListView(config, store)
            CodeBlockDisplay.gallery -> element.addTaskGalleryView(config, store)
            CodeBlockDisplay.kanban -> TODO()
            CodeBlockDisplay.table -> TODO()
        }
    }

    fun processPageCodeBlock(config: CodeBlockConfig, store: MinionStore, element: HTMLElement) {
        when(config.display) {
            CodeBlockDisplay.list -> element.addPageListView(config, store)
            CodeBlockDisplay.gallery -> element.addPageGalleryView(config, store)
            CodeBlockDisplay.kanban -> TODO()
            CodeBlockDisplay.table -> TODO()
        }
    }
}}
