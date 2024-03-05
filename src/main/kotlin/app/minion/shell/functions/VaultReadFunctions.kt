package app.minion.shell.functions

import MetadataCache
import MinionPlugin
import Vault
import app.minion.core.MinionError
import app.minion.core.model.DataviewField
import app.minion.core.model.DataviewValue
import app.minion.core.model.FileData
import app.minion.core.model.Filename
import app.minion.core.model.PageTitle
import app.minion.core.model.Tag
import app.minion.core.model.Task
import app.minion.core.store.State
import arrow.core.Either
import arrow.core.raise.either

interface VaultReadFunctions { companion object {
    suspend fun processIntoState(plugin: MinionPlugin) : Either<MinionError.VaultReadError, State> = either {
        plugin.app.vault
            .getFiles()
            .fold(StateAccumulator(plugin)) { acc, file ->
                acc.files[Filename(file.path)] = FileData(PageTitle(file.basename))
                acc
            }
            .toState()
    }
}}

/**
 * Intermediate data class to be used internally to vault processing
 */
data class StateAccumulator(
    val plugin: MinionPlugin,
    val tasks: MutableList<Task> = mutableListOf(),
    val files: MutableMap<Filename, FileData> = mutableMapOf(),
    val tagCache: MutableMap<Tag, MutableList<Filename>> = mutableMapOf(),
    val dataviewCache: MutableMap<Pair<DataviewField,DataviewValue>, MutableList<Filename>> = mutableMapOf()
) {
    fun toState() : State {
        return State(plugin, tasks, files, tagCache, dataviewCache)
    }
}
