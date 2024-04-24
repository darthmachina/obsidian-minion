package app.minion.shell.thunk

import MetadataCache
import MinionPlugin
import TFile
import Vault
import app.minion.core.model.File
import app.minion.core.model.Filename
import app.minion.core.model.MinionSettings
import app.minion.core.model.Task
import app.minion.core.store.Action
import app.minion.core.store.State
import app.minion.shell.functions.VaultReadFunctions
import app.minion.shell.functions.VaultReadFunctions.Companion.processFile
import app.minion.shell.functions.VaultReadFunctions.Companion.processIntoState
import io.kvision.redux.ActionCreator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mu.KotlinLogging

private val logger = KotlinLogging.logger("VaultThunks")

interface VaultThunks { companion object {
    fun loadInitialState(plugin: MinionPlugin, settings: MinionSettings, state: State) : ActionCreator<Action, State> {
        logger.debug { "loadVault()" }

        return { dispatch, _ ->
            CoroutineScope(Dispatchers.Unconfined).launch {
                plugin.app.vault
                    .processIntoState(plugin, settings, state)
                    .map { dispatch(Action.LoadInitialState(it)) }
                    .mapLeft {
                        logger.error { "Error loading initial state: $it" }
                        dispatch(Action.DisplayError(it))
                    }
            }
        }
    }

    fun fileModified(vault: Vault, metadataCache: MetadataCache, file: TFile) : ActionCreator<Action, State> {
        return { dispatch, state ->
            logger.debug { "fileModified() : ${file.name}" }
            if (state().settings.excludeFolders.any { file.path.startsWith(it) }) {
                logger.debug { " - in exclude folders list, ignore" }
            } else {
                CoroutineScope(Dispatchers.Unconfined).launch {
                    vault
                        .processFile(file, metadataCache)
                        .map { fileData ->
                            dispatch(Action.LoadDataForFile(fileData))
                        }
                        .mapLeft {
                            dispatch(Action.DisplayError(it))
                        }
                    // Process habits from full task list
                    //state().tasks.dispatchHabitStats(dispatch)
                }
            }
        }
    }

    fun fileDeleted(vault: Vault, metadataCache: MetadataCache, file: TFile) : ActionCreator<Action, State> {
        return { dispatch, state ->
            logger.debug { "fileDeleted() : ${file.basename}" }
            dispatch(Action.RemoveDataForFile(Filename(file.basename)))
        }
    }

    fun fileRenamed(vault: Vault, file: TFile, oldPath: String) : ActionCreator<Action, State> {
        return { dispatch, state ->
            logger.debug { "fileRenamed() : ${file.name} from '$oldPath'"}
            dispatch(Action.FileRenamed(File(oldPath), File(file.path)))
        }
    }
}}
