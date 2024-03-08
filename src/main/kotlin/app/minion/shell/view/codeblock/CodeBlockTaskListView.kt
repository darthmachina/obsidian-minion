package app.minion.shell.view.codeblock

import app.minion.core.MinionError
import app.minion.core.model.Task
import app.minion.core.store.MinionStore
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.applyDue
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.applyExcludeTags
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.applyIncludeTags
import io.kvision.state.sub
import kotlinx.dom.clear
import kotlinx.html.div
import kotlinx.html.dom.append
import org.w3c.dom.HTMLElement

interface CodeBlockTaskListView { companion object {
    fun HTMLElement.addTaskListView(config: CodeBlockConfig, store: MinionStore) {
        classList.add("ul-codeblock")
        store
            .sub { it.error }
            .subscribe { error ->
                error.map { showError(it, this) }
            }
        store
            .sub { it.tasks.applyCodeBlockConfig(config) }
            .subscribe { tasks ->
                updateTasks(tasks, this, store, config)
            }

    }

    fun showError(error: MinionError, element: HTMLElement) {
        element.clear()
        element.append.div { +error.message }
    }

    fun List<Task>.applyCodeBlockConfig(config: CodeBlockConfig) : List<Task> {
        return this
            .applyDue(config)
            .applyIncludeTags(config)
            .applyExcludeTags(config)
    }
}}