package app.minion.shell.view

import App
import MinionPlugin
import PluginSettingTab
import Setting
import app.minion.core.store.Action
import app.minion.core.store.MinionStore
import arrow.core.some
import io.kvision.core.Color
import kotlinx.html.dom.append
import kotlinx.html.h2
import mu.KotlinLogging
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
    }

    private fun createListAreaColorListSetting(containerEl: HTMLElement) : Setting {
        return Setting(containerEl)
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

    private fun createExcludeFoldersSetting(containerEl: HTMLElement) : Setting {
        return Setting(containerEl)
            .setName("Excluded folders")
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
}
