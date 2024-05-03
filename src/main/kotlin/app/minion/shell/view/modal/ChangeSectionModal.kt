package app.minion.shell.view.modal

import App
import Modal
import Setting
import app.minion.core.model.todoist.Section
import app.minion.core.model.todoist.TodoistTask
import app.minion.core.store.MinionStore
import kotlinx.dom.clear

class ChangeSectionModal(
    val task: TodoistTask,
    val sections: List<Section>,
    val store: MinionStore,
    override var app: App)
: Modal(app) {
    private var result = ""

    override fun onOpen() {
        titleEl.textContent = "Change Section"

        Setting(contentEl)
            .setName("Section")
            .addDropdown { dropdown ->
                sections.forEach {
                    dropdown.addOption(it.id, it.name)
                }
                task.section.onSome { section ->
                    dropdown.setValue(section.id)
                    result = section.id
                }
                dropdown.onChange { value ->
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
                        // TODO Update section
                        close()
                    }
            }
    }

    override fun onClose() {
        contentEl.clear()
    }
}