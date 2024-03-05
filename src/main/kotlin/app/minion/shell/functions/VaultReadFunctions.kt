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
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.toOption

interface VaultReadFunctions { companion object {
    suspend fun Vault.processIntoState(plugin: MinionPlugin) : Either<MinionError.VaultReadError, State> = either {
        this@processIntoState
            .getFiles()
            .fold(StateAccumulator(plugin)) { acc, file ->
                FileData(Filename(file.path), PageTitle(file.basename))
                    .addTags(plugin.app.metadataCache).bind()
                    .addToState(acc).bind()
           }
            .toState()
    }

    fun FileData.addTags(metadataCache: MetadataCache) : Either<MinionError.VaultReadError, FileData> = either {
        metadataCache
            .getCache(this@addTags.path.v)
            .tags
            .toOption()
            .map { it.toList() }
            .getOrElse { emptyList() }
            .map { Tag(it.tag.drop(1)) }
            .let {
                this@addTags.copy(tags = it)
            }
    }

    fun FileData.addToState(acc: StateAccumulator) : Either<MinionError.VaultReadError, StateAccumulator> = either {
        acc.addFileData(this@addToState).bind()
    }
}}





/**
 * Intermediate data class to be used internally to vault processing.
 */
data class StateAccumulator(
    val plugin: MinionPlugin,
    val tasks: MutableList<Task> = mutableListOf(),
    val files: MutableMap<Filename, FileData> = mutableMapOf(),
    val tagCache: MutableMap<Tag, MutableList<Filename>> = mutableMapOf(),
    val dataviewCache: MutableMap<Pair<DataviewField,DataviewValue>, MutableList<Filename>> = mutableMapOf()
) {
    fun addFileData(fileData: FileData) : Either<MinionError.VaultReadError, StateAccumulator> = either {
        files[fileData.path] = fileData
        addTags(fileData.tags, fileData.path)
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
