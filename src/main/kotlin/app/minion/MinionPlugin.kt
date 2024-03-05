import app.minion.core.JsJodaTimeZoneModule
import app.minion.core.store.State
import app.minion.core.store.reducer
import app.minion.shell.thunk.VaultThunks
import io.kvision.redux.createTypedReduxStore
import mu.KotlinLogging
import mu.KotlinLoggingConfiguration
import mu.KotlinLoggingLevel

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
            this,
            emptyList(),
            emptyMap(),
            emptyMap(),
            emptyMap()
        )
    )

    override fun onload() {
        logger.debug { "onload()" }

        app.workspace.onLayoutReady {
            logger.debug { "onLayoutReady()" }

            store.dispatch(VaultThunks.loadInitialState(this))
        }
    }
}
