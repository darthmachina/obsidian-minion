package app.minion.shell.functions

import MetadataCache
import MinionPlugin
import TFile
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
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.toOption

interface VaultReadFunctions { companion object {
    suspend fun processIntoState(plugin: MinionPlugin) : Either<MinionError.VaultReadError, State> = either {
        plugin.app.vault
            .getFiles()
            .fold(StateAccumulator(plugin)) { acc, file ->
                acc
                    .addFile(file)
                    .map {
                        plugin.app.metadataCache
                            .getCache(file.path)
                            .tags
                            .toOption()
                            .map { it.toList() }
                            .getOrElse { emptyList() }
                            .map { Tag(it.tag.drop(1)) }
                            .let {
                                acc.addTags(it, Filename(file.path)).bind()
                            }
                    }
                    .bind()
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
    fun addFile(file: TFile) : Either<MinionError.VaultReadError, StateAccumulator> = either {
        files[Filename(file.path)] = FileData(PageTitle(file.basename))
        this@StateAccumulator
    }

    fun addTags(tags: List<Tag>, filename: Filename) : Either<MinionError.VaultReadError, StateAccumulator> = either {
        tags.forEach { addTag(it, filename).bind() }
        this@StateAccumulator
    }

    fun addTag(tag: Tag, filename: Filename) : Either<MinionError.VaultReadError, StateAccumulator> = either {
        if (!tagCache.containsKey(tag)) {
            tagCache[tag] = mutableListOf()
        }
        tagCache[tag]
            ?.add(filename)
            ?: MinionError.VaultReadError("Error adding filename to tagCache")
        this@StateAccumulator
    }

    fun toState() : State {
        return State(plugin, tasks, files, tagCache, dataviewCache)
    }
}
