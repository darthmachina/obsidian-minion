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
import app.minion.core.model.Tag
import app.minion.core.model.Task
import app.minion.core.store.State
import app.minion.shell.functions.TaskReadFunctions.Companion.processFileTasks
import arrow.core.Either
import arrow.core.None
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.toOption
import kotlinx.coroutines.await
import mu.KotlinLogging

private val logger = KotlinLogging.logger("VaultReadFunctions")

interface VaultReadFunctions { companion object {

    suspend fun Vault.processIntoState(plugin: MinionPlugin) : Either<MinionError, State> = either {
        this@processIntoState
            .getFiles()
            .filter { it.path.endsWith(".md") }
            .fold(StateAccumulator(plugin)) { acc, file ->
                logger.debug { "Processing ${file.path}" }
                this@processIntoState
                    .processFile(file, plugin.app.metadataCache).bind()
                    .addToState(acc).bind()
           }
            .toState()
    }

    suspend fun Vault.processFile(file: TFile, metadataCache: MetadataCache) : Either<MinionError, FileData> = either {
        FileData(Filename(file.basename))
            .addTags(metadataCache).bind()
            .addBacklinks(metadataCache).bind()
            .processFileContents(this@processFile, metadataCache).bind()
    }

    fun FileData.addTags(metadataCache: MetadataCache) : Either<MinionError, FileData> = either {
        logger.debug { "FileData.addTags()" }
        metadataCache
            .getCache(this@addTags.path.fullName())
            .toOption()
            .map {
                it.tags?.toList() ?: emptyList()
            }
            .map {tagCache ->
                tagCache
                    .map {
                        Tag(it.tag.drop(1))
                    }
                    .distinct()
                    .let {
                        this@addTags.copy(tags = it)
                    }
            }
            .getOrElse { this@addTags }// No tags in the file, just return
    }

    fun FileData.addBacklinks(metadataCache: MetadataCache) : Either<MinionError, FileData> = either {
        logger.debug { "FileData.addBacklinks()" }
        metadataCache
            .getCache(this@addBacklinks.path.fullName())
            .toOption()
            .map { it.links?.toList() ?: emptyList() }
            .map { linkCache ->
                linkCache
                    .map {
                        Filename(it.link)
                    }
                    .distinct()
                    .let {
                        this@addBacklinks.copy(outLinks = it)
                    }
            }
            .getOrElse { this@addBacklinks }
    }

    suspend fun FileData.processFileContents(vault: Vault, metadataCache: MetadataCache) : Either<MinionError, FileData> = either {
        logger.debug { "processFileContents: ${this@processFileContents.path.v}" }
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
                                .filter { !it.completed }
                        )
                    }
                    .await()
            }.bind()
    }

    fun String.pullOutDataviewFields() : Either<MinionError, Map<DataviewField, DataviewValue>> = either {
        logger.debug { "pullOutDataviewFields()" }
        dataviewRegex.findAll(this@pullOutDataviewFields)
            .associate { DataviewField(it.groupValues[1]) to DataviewValue(it.groupValues[2]) }
    }

    fun FileData.addToState(acc: StateAccumulator) : Either<MinionError, StateAccumulator> = either {
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
    val tagCache: MutableMap<Tag, MutableSet<Filename>> = mutableMapOf(),
    val dataviewCache: MutableMap<Pair<DataviewField,DataviewValue>, MutableSet<Filename>> = mutableMapOf(),
    val backlinkCache: MutableMap<Filename, MutableSet<Filename>> = mutableMapOf()
) {
    fun addFileData(fileData: FileData) : Either<MinionError, StateAccumulator> = either {
        files[fileData.path] = fileData
        addTags(fileData.tags, fileData.path)
        addBacklinks(fileData.outLinks, fileData.path)
        addDataview(fileData.dataview, fileData.path)
        addTasks(fileData.tasks)
        this@StateAccumulator
    }

    fun addTags(tags: List<Tag>, filename: Filename) : Either<MinionError, StateAccumulator> = either {
        logger.debug { "addTags()" }
        tags.forEach { addTag(it, filename).bind() }
        this@StateAccumulator
    }

    fun addTag(tag: Tag, filename: Filename) : Either<MinionError, StateAccumulator> = either {
        logger.debug { "addTag()" }
        if (!tagCache.containsKey(tag)) {
            tagCache[tag] = mutableSetOf()
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
    fun addBacklinks(outlinks: List<Filename>, filename: Filename) : Either<MinionError, StateAccumulator> = either {
        logger.debug { "addBacklinks()" }
        outlinks.forEach { link ->
            if (!backlinkCache.containsKey(link)) {
                backlinkCache[link] = mutableSetOf()
            }

            backlinkCache[link]
                ?.add(filename)
                ?: MinionError.VaultReadError("Error adding $filename to backlinksCache")
        }
        this@StateAccumulator
    }

    fun addDataview(dataview: Map<DataviewField, DataviewValue>, filename: Filename) : Either<MinionError, StateAccumulator> = either {
        logger.debug { "addDataview()" }
        dataview.entries.forEach { entry ->
            val dataviewPair = entry.key to entry.value
            if (!dataviewCache.containsKey(dataviewPair)) {
                dataviewCache[dataviewPair] = mutableSetOf()
            }

            dataviewCache[dataviewPair]
                ?.add(filename)
                ?: MinionError.VaultReadError("Error adding $filename to dataviewCache")
        }
        this@StateAccumulator
    }

    fun addTasks(tasks: List<Task>) : Either<MinionError, StateAccumulator> = either {
        logger.debug { "addTasks()" }
        this@StateAccumulator.tasks.addAll(tasks)
        this@StateAccumulator
    }

    fun toState() : State {
        return State(plugin, None, tasks, files, tagCache, dataviewCache, backlinkCache)
    }
}
