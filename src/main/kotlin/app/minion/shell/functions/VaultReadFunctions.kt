package app.minion.shell.functions

import MetadataCache
import MinionPlugin
import Vault
import app.minion.core.MinionError
import app.minion.core.model.DataviewField
import app.minion.core.model.DataviewValue
import app.minion.core.model.FileData
import app.minion.core.model.Filename
import app.minion.core.model.Tag
import app.minion.core.model.Task
import app.minion.core.store.State
import app.minion.shell.functions.TaskReadFunctions.Companion.processFileTasks
import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.toOption
import kotlinx.coroutines.await
import mu.KotlinLogging

private val logger = KotlinLogging.logger("VaultReadFunctions")

interface VaultReadFunctions { companion object {
    suspend fun Vault.processIntoState(plugin: MinionPlugin) : Either<MinionError.VaultReadError, State> = either {
        this@processIntoState
            .getFiles()
            .fold(StateAccumulator(plugin)) { acc, file ->
                logger.debug { "Processing ${file.path}" }
                FileData(Filename(file.basename))
                    .addTags(plugin.app.metadataCache).bind()
                    .addBacklinks(plugin.app.metadataCache).bind()
                    .processFileContents(this@processIntoState, plugin.app.metadataCache).bind()
                    .addToState(acc).bind()
           }
            .toState()
    }

    fun FileData.addTags(metadataCache: MetadataCache) : Either<MinionError.VaultReadError, FileData> = either {
        logger.debug { "FileData.addTags()" }
        metadataCache
            .getCache(this@addTags.path.fullName())
            .tags
            .toOption()
            .map { it.toList() }
            .getOrElse { emptyList() }
            .map { Tag(it.tag.drop(1)) }
            .distinct()
            .let {
                this@addTags.copy(tags = it)
            }
    }

    fun FileData.addBacklinks(metadataCache: MetadataCache) : Either<MinionError.VaultReadError, FileData> = either {
        logger.debug { "FileData.addBacklinks()" }
        metadataCache
            .getCache(this@addBacklinks.path.fullName())
            .links
            .toOption()
            .map { it.toList() }
            .getOrElse { emptyList() }
            .map { Filename(it.link) }
            .distinct()
            .let {
                this@addBacklinks.copy(outLinks = it)
            }
    }

    suspend fun FileData.processFileContents(vault: Vault, metadataCache: MetadataCache) : Either<MinionError.VaultReadError, FileData> = either {
        metadataCache
            .getFirstLinkpathDest(this@processFileContents.path.v, "")
            .toOption()
            .toEither {
                MinionError.VaultReadError("Error reading ${this@processFileContents.path.v}")
            }
            .map { tfile ->
                vault
                    .read(tfile)
                    .then { contents ->
                        // Process Tasks
                        this@processFileContents.copy(
                            dataview = contents.pullOutDataviewFields().bind(),
                            tasks = contents.processFileTasks(this@processFileContents.path, metadataCache)
                                .mapLeft { MinionError.VaultReadError(it.message, it.throwable, parent = it.toOption()) }
                                .bind()
                        )
                    }
                    .await()
            }.bind()
    }

    fun String.pullOutDataviewFields() : Either<MinionError.VaultReadError, Map<DataviewField, DataviewValue>> = either {
        logger.debug { "pullOutDataviewFields()" }
        dataviewRegex.findAll(this@pullOutDataviewFields)
            .associate { DataviewField(it.groupValues[1]) to DataviewValue(it.groupValues[2]) }
    }

    fun FileData.addToState(acc: StateAccumulator) : Either<MinionError.VaultReadError, StateAccumulator> = either {
        acc.addFileData(this@addToState).bind()
    }
}}

/**
 * Intermediate data class to be used internally for vault processing.
 */
data class StateAccumulator(
    val plugin: MinionPlugin,
    val tasks: MutableList<Task> = mutableListOf(),
    val files: MutableMap<Filename, FileData> = mutableMapOf(),
    val tagCache: MutableMap<Tag, MutableList<Filename>> = mutableMapOf(),
    val dataviewCache: MutableMap<Pair<DataviewField,DataviewValue>, MutableList<Filename>> = mutableMapOf(),
    val backlinkCache: MutableMap<Filename, MutableList<Filename>> = mutableMapOf()
) {
    fun addFileData(fileData: FileData) : Either<MinionError.VaultReadError, StateAccumulator> = either {
        files[fileData.path] = fileData
        addTags(fileData.tags, fileData.path)
        addBacklinks(fileData.outLinks, fileData.path)
        addDataview(fileData.dataview, fileData.path)
        addTasks(fileData.tasks)
        this@StateAccumulator
    }

    fun addTags(tags: List<Tag>, filename: Filename) : Either<MinionError.VaultReadError, StateAccumulator> = either {
        logger.debug { "addTags()" }
        tags.forEach { addTag(it, filename).bind() }
        this@StateAccumulator
    }

    fun addTag(tag: Tag, filename: Filename) : Either<MinionError.VaultReadError, StateAccumulator> = either {
        logger.debug { "addTag()" }
        if (!tagCache.containsKey(tag)) {
            tagCache[tag] = mutableListOf()
        }
        tagCache[tag]
            ?.add(filename)
            ?: MinionError.VaultReadError("Error adding $filename to tagCache for $tag")
        this@StateAccumulator
    }

    /**
     * Takes a list of outlinks for a file and modifies backlinkCache accordingly.
     *
     * Each entry in outlinks is a key in backlinksCache and the file being processed is added to the target list
     */
    fun addBacklinks(outlinks: List<Filename>, filename: Filename) : Either<MinionError.VaultReadError, StateAccumulator> = either {
        logger.debug { "addBacklinks()" }
        outlinks.forEach { link ->
            if (!backlinkCache.containsKey(link)) {
                backlinkCache[link] = mutableListOf()
            }

            backlinkCache[link]
                ?.add(filename)
                ?: MinionError.VaultReadError("Error adding $filename to backlinksCache")
        }
        this@StateAccumulator
    }

    fun addDataview(dataview: Map<DataviewField, DataviewValue>, filename: Filename) : Either<MinionError.VaultReadError, StateAccumulator> = either {
        logger.debug { "addDataview()" }
        dataview.entries.forEach { entry ->
            val dataviewPair = entry.key to entry.value
            if (!dataviewCache.containsKey(dataviewPair)) {
                dataviewCache[dataviewPair] = mutableListOf()
            }

            dataviewCache[dataviewPair]
                ?.add(filename)
                ?: MinionError.VaultReadError("Error adding $filename to dataviewCache")
        }
        this@StateAccumulator
    }

    fun addTasks(tasks: List<Task>) : Either<MinionError.VaultReadError, StateAccumulator> = either {
        logger.debug { "addTasks()" }
        this@StateAccumulator.tasks.addAll(tasks)
        this@StateAccumulator
    }

    fun toState() : State {
        return State(plugin, tasks, files, tagCache, dataviewCache, backlinkCache)
    }
}
