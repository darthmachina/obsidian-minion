package app.minion.shell.view.modal

import App
import Modal
import Setting
import app.minion.core.functions.TaskTagFunctions.Companion.findTagWithPrefix
import app.minion.core.model.KanbanStatus
import app.minion.core.model.Task
import app.minion.core.store.MinionStore
import kotlinx.dom.clear

class KanbanStatusSelectModal(val store: MinionStore, val task: Task, override var app: App) : Modal(app) {
    private var result = KanbanStatus.ICEBOX.tag

    override fun onOpen() {
        titleEl.textContent = "Select Status"

        Setting(contentEl)
            .setName("Status")
            .addDropdown { dropdown ->
                KanbanStatus.entries
                    .forEach {
                        dropdown.addOption(it.tag, it.display)
                    }
                dropdown
                    .onChange { value ->
                        result = value
                        result
                    }
                task
                    .findTagWithPrefix("kanban")
                    .map {
                        dropdown.setValue(it.v.drop(7))
                        result = it.v.drop(7)
                    }
            }

        Setting(contentEl)
            .addButton { button ->
                button
                    .setButtonText("Save")
                    .setCta()
                    .onClick {
                        //store.dispatch(TaskThunks.changeKanbanStatus(app, task, result))
                        close()
                    }
            }
    }

    override fun onClose() {
        contentEl.clear()
    }
}
