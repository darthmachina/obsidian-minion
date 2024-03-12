package app.minion.shell.thunk

import MetadataCache
import Vault
import app.minion.core.MinionError
import app.minion.core.functions.PageFunctions.Companion.replaceDataviewValue
import app.minion.core.model.DataviewField
import app.minion.core.model.DataviewValue
import app.minion.core.model.FileData
import app.minion.core.store.Action
import app.minion.core.store.State
import app.minion.shell.functions.VaultReadFunctions.Companion.readFile
import app.minion.shell.functions.VaultWriteFunctions.Companion.writeToVault
import arrow.core.raise.either
import arrow.core.toOption
import io.kvision.redux.ActionCreator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mu.KotlinLogging

private val logger = KotlinLogging.logger("PageThunks")

interface PageThunks { companion object {
    fun updateDataviewValue(
        file: FileData,
        field: DataviewField,
        oldValue: DataviewValue,
        newValue: DataviewValue,
        vault: Vault,
        metadataCache: MetadataCache
    ) : ActionCreator<Action, State> {
        logger.info { "updateDataviewValue" }
        return { dispatch, _ ->
            CoroutineScope(Dispatchers.Unconfined).launch {
                either {
                    vault
                        .readFile(file, metadataCache).bind()
                        .replaceDataviewValue(field, oldValue, newValue).bind()
                        .writeToVault(file, vault, metadataCache)
                }
           }
        }
    }
}}
