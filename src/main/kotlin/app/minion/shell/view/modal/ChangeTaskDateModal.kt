package app.minion.shell.view.modal

import App
import Modal
import Setting
import app.minion.core.functions.DateTimeFunctions
import app.minion.core.functions.DateTimeFunctions.Companion.asString
import app.minion.core.model.Task
import app.minion.core.store.MinionStore
import mu.KotlinLogging

private val logger = KotlinLogging.logger("ChangeTaskDateModal")

class ChangeTaskDateModal(val task: Task, val store: MinionStore, override var app: App) : Modal(app) {
    private var result = ""

    override fun onOpen() {
        titleEl.textContent = "Change Task Date"

        Setting(contentEl)
            .setName("Date")
            .addText { component ->
                component.inputEl.type = "date"
                task.dueDate.map {
                    component.setValue(it.asString())
                }
                component.onChange { value ->
                    logger.debug { "Update value to $value" }
                    result = value
                    result
                }
            }

        Setting(contentEl)
            .addButton { button ->
                button
                    .setButtonText("Save")
                    .setCta()
                    .onClick {
                        DateTimeFunctions.parseDateTime(result)
                            .map { store.dispatch(TaskThunks.changeDate(task, app, it)) }
                            .mapLeft {
                                logger.warn { "Cannot parse date $it" }
                            }
                        close()
                    }
            }
    }
}