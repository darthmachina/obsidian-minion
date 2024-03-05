package app.minion.shell.thunk

import MinionPlugin
import app.minion.core.store.Action
import app.minion.core.store.State
import app.minion.shell.functions.VaultReadFunctions
import io.kvision.redux.ActionCreator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mu.KotlinLogging

private val logger = KotlinLogging.logger {  }

interface VaultThunks { companion object {
    fun loadInitialState(plugin: MinionPlugin) : ActionCreator<Action, State> {
        logger.debug { "loadVault()" }

        return { dispatch, _ ->
            CoroutineScope(Dispatchers.Unconfined).launch {
                VaultReadFunctions
                    .processIntoState(plugin)
                    .map { dispatch(Action.LoadInitialState(it)) }
                    .mapLeft {
                        logger.error { "Error loading initial state: $it" }
                        dispatch(Action.DisplayError(it.message))
                    }
            }
        }
    }
}}
