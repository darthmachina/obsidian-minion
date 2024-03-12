package app.minion.shell.view.modal

import App
import Modal
import Setting
import app.minion.core.model.DataviewField
import app.minion.core.model.DataviewValue
import app.minion.core.model.FileData
import app.minion.core.store.MinionStore
import app.minion.shell.thunk.PageThunks
import mu.KotlinLogging

const val NEW = "New"

private val logger = KotlinLogging.logger("UpdateDataviewValue")

class UpdateDataviewValue(
    private val fileData: FileData,
    private val field: String,
    private val current: DataviewValue,
    private val dataviewValues: Set<DataviewValue>,
    val store: MinionStore,
    override var app: App
) : Modal(app) {
    private var dropdownResult = ""
    private var textResult = ""
    private var result = ""

    override fun onOpen() {
        titleEl.textContent = "Update $field"

        val dropdown = Setting(contentEl)
        val text = Setting(contentEl)

        dropdown
            .setName("$field Value")
            .addDropdown { component ->
                component.addOption(NEW, NEW)
                dataviewValues.forEach {
                    component.addOption(it.v, it.v)
                }
                component.onChange { value ->
                    if (value == NEW) {
                        dropdownResult = value
                        result = textResult
                        text.setDisabled(false)
                    } else {
                        dropdownResult = value
                        result = value
                        text.setDisabled(true)
                    }
                }
                component.setValue(current.v)
            }

        text
            .setName("New Value")
            .addText { component ->
                component
                    .setPlaceholder("Create new value")
                    .onChange {value ->
                        textResult = value
                        if (dropdownResult == NEW) {
                            result = value
                        }
                    }
                    .setDisabled(true)
            }

        Setting(contentEl)
            .addButton { component ->
                component
                    .setButtonText("Save")
                    .setCta()
                    .onClick {
                        logger.debug { "Updating $field value to $result" }
                        store.dispatch(PageThunks.updateDataviewValue(
                            fileData,
                            DataviewField(field),
                            current,
                            DataviewValue(result),
                            app.vault,
                            app.metadataCache
                        ))
                        close()
                    }
            }
    }
}
