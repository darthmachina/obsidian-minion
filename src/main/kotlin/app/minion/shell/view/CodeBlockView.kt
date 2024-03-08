package app.minion.shell.view

import MarkdownPostProcessorContext
import app.minion.core.store.MinionStore
import app.minion.shell.view.codeblock.CodeBlockConfig
import app.minion.shell.view.codeblock.CodeBlockDisplay
import app.minion.shell.view.codeblock.CodeBlockQuery
import app.minion.shell.view.codeblock.CodeBlockTaskListView.Companion.addTaskListView
import net.mamoe.yamlkt.Yaml
import org.w3c.dom.HTMLElement

interface CodeBlockView { companion object {
    fun processCodeBlock(store: MinionStore): (source: String, element: HTMLElement, context: MarkdownPostProcessorContext) -> Unit {
        return { source, element, _ ->
            val config = Yaml.decodeFromString(CodeBlockConfig.serializer(), source)

            when(config.query) {
                CodeBlockQuery.pages -> processPageCodeBlock(config, store, element)
                CodeBlockQuery.tasks -> processTaskCodeBlock(config, store, element)
            }
        }
    }

    fun processTaskCodeBlock(config: CodeBlockConfig, store: MinionStore, element: HTMLElement) {
        when(config.display) {
            CodeBlockDisplay.list -> processTaskListView(config, store, element)
            CodeBlockDisplay.gallery -> TODO()
            CodeBlockDisplay.kanban -> TODO()
            CodeBlockDisplay.table -> TODO()
        }
    }

    fun processPageCodeBlock(config: CodeBlockConfig, store: MinionStore, element: HTMLElement) {
        when(config.display) {
            CodeBlockDisplay.list -> TODO()
            CodeBlockDisplay.gallery -> TODO()
            CodeBlockDisplay.kanban -> TODO()
            CodeBlockDisplay.table -> TODO()
        }
    }

    fun processTaskListView(config: CodeBlockConfig, store: MinionStore, element: HTMLElement) {
        element.addTaskListView(config, store)
    }
}}
