package app.minion.shell.view

import App
import MinionPlugin
import PluginSettingTab
import Setting
import app.minion.core.model.DataviewField
import app.minion.core.model.PageTaskField
import app.minion.core.model.PageTaskFieldType
import app.minion.core.store.Action
import app.minion.core.store.MinionStore
import arrow.core.some
import arrow.core.toOption
import io.kvision.core.Color
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.h2
import kotlinx.html.hr
import kotlinx.html.span
import mu.KotlinLogging
import mu.KotlinLoggingLevel
import org.w3c.dom.HTMLElement

private val logger = KotlinLogging.logger("MinionSettingsTab")

class MinionSettingsTab(
    override var app: App,
    plugin: MinionPlugin,
    val store: MinionStore
) : PluginSettingTab(app, plugin) {
    override fun display() {
        while(containerEl.firstChild != null) {
            containerEl.lastChild?.let { containerEl.removeChild(it) }
        }

        containerEl.append.h2 { +"Underling Settings "}
        createListAreaColorListSetting(containerEl)
        createExcludeFoldersSetting(containerEl)
        createLogLevelSettings(containerEl)
        createPageTaskFieldSettings(containerEl)
        createTodoistApiTokenSettings(containerEl)
        containerEl.append.div {
            hr {  }
            span(classes = "mi-bold") { +"Note: " }
            span { +"Items marked with a * require a plugin restart" }
        }
    }

    private fun createListAreaColorListSetting(containerEl: HTMLElement) {
        Setting(containerEl)
            .setName("Life Area Colors")
            .setDesc("List of colors to use for Life Areas")
            .addTextArea { text ->
                val textVersion = store.store.state.settings.lifeAreas
                    .map { entry -> "${entry.key}:${entry.value.color}" }
                    .joinToString("\n")
                text.setPlaceholder("'tag:hex_color' separated by newlines")
                    .setValue(textVersion)
                    .onChange { value ->
                        value
                            .split("\n")
                            .map { it.split(":") }
                            .associate { Pair(it[0], Color(it[1])) }
                            .let {
                                store.dispatch(Action.UpdateSettings(lifeAreas = it.some()))
                            }
                    }
            }
    }

    private fun createExcludeFoldersSetting(containerEl: HTMLElement) {
        Setting(containerEl)
            .setName("Excluded folders*")
            .setDesc("Folders to exclude from processing")
            .addTextArea { text ->
                val textVersion = store.store.state.settings.excludeFolders.joinToString("\n")
                text.setPlaceholder("folders separated by newlines")
                    .setValue(textVersion)
                    .onChange { value ->
                        value.split("\n")
                            .toSet()
                            .let {
                                logger.debug { "Dispatching UpdateSettings: $it" }
                                store.dispatch(Action.UpdateSettings(excludeFolders = it.some()))
                            }
                    }
            }
    }

    private fun createLogLevelSettings(containerEl: HTMLElement) {
        Setting(containerEl)
            .setName("Log Level")
            .setDesc("Set the log level")
            .addDropdown { dropdown ->
                KotlinLoggingLevel.values().forEach { level ->
                    dropdown.addOption(level.name, level.name)
                }
                dropdown.setValue(store.store.state.settings.logLevel.name)
                dropdown.onChange {
                    logger.debug { "onChange(): $it" }
                    store.dispatch(Action.UpdateSettings(logLevel = KotlinLoggingLevel.valueOf(it).some()))
                }
            }
    }

    private fun createPageTaskFieldSettings(containerEl: HTMLElement) {
        Setting(containerEl)
            .setName("Page Task Tag Fields*")
            .setDesc("Page level Dataview fields containing Task tags")
            .addTextArea { text ->
                text
                    .setPlaceholder("Dataview fields separated by newlines")
                    .setValue(store.store.state.settings.pageTaskFields.map { it.field.v }.joinToString("\n"))
                    .onChange { value ->
                        value
                            .split("\n")
                            .map {
                                PageTaskField(DataviewField(it), PageTaskFieldType.TAG)
                            }
                            .let {
                                logger.debug { "Dispatching UpdateSettings: $it" }
                                store.dispatch(Action.UpdateSettings(pageTaskFields = it.some()))
                            }
                    }
            }
    }

    private fun createTodoistApiTokenSettings(containerEl: HTMLElement) {
        Setting(containerEl)
            .setName("Todoist API Token")
            .setDesc("API token for accessing Todoist")
            .addText { text ->
                text
                    .setPlaceholder("API token")
                    .setValue(store.store.state.settings.todoistApiToken)
                    .onChange { value ->
                        store.dispatch(Action.UpdateSettings(todoistApiToken = value.some()))
                    }
            }
    }
}
