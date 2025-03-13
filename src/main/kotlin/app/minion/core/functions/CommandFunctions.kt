package app.minion.core.functions

import app.minion.core.store.MinionStore
import app.minion.core.store.StateFunctions.Companion.findTaskAtCursor
import app.minion.core.store.StateFunctions.Companion.runWithPlugin
import app.minion.shell.thunk.TaskThunks
import app.minion.shell.view.modal.KanbanStatusSelectModal
import mu.KotlinLogging

private val logger = KotlinLogging.logger("CommandFunctions")

interface CommandFunctions { companion object {
    fun changeTaskStatusAtLine(store: MinionStore) {
        store.runWithPlugin { plugin ->
            store.findTaskAtCursor().map { task ->
                KanbanStatusSelectModal(store, task, plugin.app).open()
            }
        }
    }

    fun completeTaskAtLine(store: MinionStore) {
        store.runWithPlugin { plugin ->
            store.findTaskAtCursor().map { task ->
                store.dispatch(TaskThunks.completeTask(plugin.app, task))
            }
        }
    }
}}