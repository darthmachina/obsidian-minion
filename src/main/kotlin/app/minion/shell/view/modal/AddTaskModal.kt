package app.minion.shell.view.modal

import App
import Modal
import Setting
import app.minion.core.functions.ProjectFunctions.Companion.findInbox
import app.minion.core.model.Content
import app.minion.core.store.MinionStore
import app.minion.shell.thunk.TodoistThunks
import kotlinx.dom.clear
import mu.KotlinLogging

private val logger = KotlinLogging.logger("AddTaskModal")

class AddTaskModal(val store: MinionStore, override var app: App) : Modal(app) {
    private var result = ""

    override fun onOpen() {
        titleEl.textContent = "Add Task to Inbox"

        Setting(contentEl)
            .setName("Task")
            .addText { text ->
                text
                    .setPlaceholder("Task content")
                    .onChange {
                        result = text.getValue()
                        result
                    }
            }

        Setting(contentEl)
            .addButton { button ->
                button
                    .setButtonText("Save")
                    .setCta()
                    .onClick {
                        store.store.state.projects
                            .findInbox()
                            .onRight { inbox ->
                                store.dispatch(TodoistThunks.addToProject(
                                    Content(result),
                                    inbox,
                                    store.store.state.settings.todoistApiToken
                                ))
                            }
                            .onLeft {
                                logger.error { "Cannot find inbox: $it" }
                            }
                        close()
                    }
            }
    }

    override fun onClose() {
        contentEl.clear()
    }
}