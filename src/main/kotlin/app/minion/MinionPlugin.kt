import app.minion.core.JsJodaTimeZoneModule
import app.minion.core.functions.SettingsFunctions
import app.minion.core.model.MinionSettings
import app.minion.core.model.MinionSettings1
import app.minion.core.store.Action
import app.minion.core.store.State
import app.minion.core.store.reducer
import app.minion.shell.thunk.VaultThunks
import app.minion.shell.view.CodeBlockView
import app.minion.shell.view.MinionSettingsTab
import app.minion.shell.view.codeblock.CodeBlockConfig
import arrow.core.None
import arrow.core.some
import arrow.core.toOption
import io.kvision.redux.createTypedReduxStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import mu.KotlinLogging
import mu.KotlinLoggingConfiguration
import mu.KotlinLoggingLevel
import net.mamoe.yamlkt.Yaml

private val logger = KotlinLogging.logger {  }

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("default")
class MinionPlugin(app: App, manifest: PluginManifest) : Plugin(app, manifest) {
    init {
        KotlinLoggingConfiguration.LOG_LEVEL = KotlinLoggingLevel.DEBUG
        @Suppress("UNUSED_VARIABLE") val jsJodaTz = JsJodaTimeZoneModule // Required for JS TimeZones
    }

    private val store = createTypedReduxStore(
        ::reducer,
        State(
            this.some(),
            MinionSettings.default(),
            None,
            emptyList(),
            emptyMap(),
            emptyMap(),
            emptyMap(),
            emptyMap(),
            emptyMap()
        )
    )

    override fun onload() {
        logger.debug { "onload()" }

        addSettingTab(MinionSettingsTab(app, this, store))
        registerMarkdownCodeBlockProcessor("minion", CodeBlockView.processCodeBlock(store))

        app.workspace.onLayoutReady {
            logger.debug { "onLayoutReady()" }
            CoroutineScope(Dispatchers.Unconfined).launch {
                loadSettings()
                store.dispatch(VaultThunks.loadInitialState(this@MinionPlugin, store.store.state.settings))
            }

            registerEvent(
                app.metadataCache.on("changed") { file ->
                    store.dispatch(VaultThunks.fileModified(app.vault, app.metadataCache, file))
                }
            )
            registerEvent(
                app.vault.on("delete") { file ->
                    if (file is TFile) {
                        store.dispatch(VaultThunks.fileDeleted(app.vault, app.metadataCache, file))
                    }
                }
            )
            registerEvent(
                app.vault.on("rename") { file, oldPath ->
                    if (file is TFile) {
                        store.dispatch(VaultThunks.fileRenamed(app.vault, file, oldPath))
                    }
                }
            )

        }
    }

    private suspend fun loadSettings() {
        loadData().then { result ->
            SettingsFunctions.loadFromJson(result.toOption())
                .map { store.dispatch(Action.LoadSettings(settings = it)) }
        }.await()
    }
}
