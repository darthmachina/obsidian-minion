package app.minion.shell.functions

import MetadataCache
import MinionPlugin
import TFile
import Vault
import app.minion.core.MinionError
import app.minion.core.functions.TaskFunctions.Companion.maybeAddDataviewValues
import app.minion.core.functions.dataviewRegex
import app.minion.core.model.DataviewField
import app.minion.core.model.DataviewValue
import app.minion.core.model.File
import app.minion.core.model.FileData
import app.minion.core.model.Filename
import app.minion.core.model.MinionSettings
import app.minion.core.model.Tag
import app.minion.core.model.Task
import app.minion.core.store.State
import app.minion.shell.functions.TaskReadFunctions.Companion.processFileTasks
import app.minion.shell.functions.VaultReadFunctions.Companion.mapToFieldCache
import arrow.core.Either
import arrow.core.None
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.toOption
import kotlinx.coroutines.await
import mu.KotlinLogging

private val logger = KotlinLogging.logger("VaultReadFunctions")

interface VaultReadFunctions { companion object {
    suspend fun Vault.processIntoState(plugin: MinionPlugin, settings: MinionSettings)
    : Either<MinionError, State> = either {
        this@processIntoState
            .getFiles()
            .filter { tfile ->
                val exclude = settings.excludeFolders.any {
                    tfile.path.startsWith(it)
                }
                tfile.path.endsWith(".md") &&
                        !exclude
            }
            .fold(StateAccumulator()) { acc, file ->
                logger.debug { "Processing ${file.path}" }
                this@processIntoState
                    .processFile(file, plugin.app.metadataCache).bind()
                    .addToState(acc, settings).bind()
           }
            .toState(settings, plugin).bind()
    }

    suspend fun Vault.readFile(fileData: FileData, metadataCache: MetadataCache)
    : Either<MinionError, String> = either {
        metadataCache
            .getFirstLinkpathDest(fileData.path.v, "")
            .toOption()
            .toEither {
                MinionError.VaultReadError("Error reading ${fileData.path.v}")
            }
            .map { tfile ->
                this@readFile.read(tfile).await()
            }
            .bind()
    }

    suspend fun Vault.processFile(file: TFile, metadataCache: MetadataCache) : Either<MinionError, FileData> = either {
        FileData(Filename(file.basename), File(file.path))
            .addTags(metadataCache).bind()
            .addOutlinks(metadataCache).bind()
            .processFileContents(this@processFile, metadataCache).bind()
    }

    fun FileData.addTags(metadataCache: MetadataCache) : Either<MinionError, FileData> = either {
        logger.debug { "FileData.addTags(): ${this@addTags.path}" }
        metadataCache
            .getCache(this@addTags.path.v)
            .toOption()
            .map {
                logger.debug { "- pulling out tags from metadata" }
                it.tags?.toList() ?: emptyList()
            }
            .map {tagCache ->
                tagCache
                    .map {
                        logger.debug { "- creating Tag from ${it.tag}" }
                        Tag(it.tag.drop(1).trim())
                    }
                    .distinct()
                    .let {
                        logger.debug { "- new tag list $it" }
                        this@addTags.copy(tags = it)
                    }
            }
            .getOrElse { this@addTags }// No tags in the file, just return
    }

    fun FileData.addOutlinks(metadataCache: MetadataCache) : Either<MinionError, FileData> = either {
        logger.debug { "FileData.addBacklinks()" }
        metadataCache
            .getCache(this@addOutlinks.path.v)
            .toOption()
            .map { it.links?.toList() ?: emptyList() }
            .map { linkCache ->
                linkCache
                    .map {
                        Filename(it.link)
                    }
                    .distinct()
                    .let {
                        this@addOutlinks.copy(outLinks = it)
                    }
            }
            .getOrElse { this@addOutlinks }
    }

    suspend fun FileData.processFileContents(vault: Vault, metadataCache: MetadataCache)
    : Either<MinionError, FileData> = either {
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
                            tasks = contents
                                .processFileTasks(
                                    this@processFileContents.path,
                                    this@processFileContents.name,
                                    metadataCache
                                )
                                .mapLeft {
                                    MinionError.VaultReadError(it.message, it.throwable, parent = it.toOption())
                                }
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
            .associate { DataviewField(it.groupValues[1]) to DataviewValue(it.groupValues[2].trim()) }
    }

    fun Set<Pair<DataviewField, DataviewValue>>.mapToFieldCache()
    : Either<MinionError, Map<DataviewField, Set<DataviewValue>>> = either {
        this@mapToFieldCache
            .groupBy { it.first }
            .mapValues { entry -> entry.value.map { it.second }.toSet() }
    }

    fun FileData.addToState(acc: StateAccumulator, settings: MinionSettings)
    : Either<MinionError, StateAccumulator> = either {
        acc.addFileData(this@addToState, settings).bind()
    }
}}

/**
 * Intermediate data class to be used internally for vault processing.
 */
data class StateAccumulator(
    val tasks: MutableList<Task> = mutableListOf(),
    val files: MutableMap<Filename, FileData> = mutableMapOf(),
    val tagCache: MutableMap<Tag, MutableSet<Filename>> = mutableMapOf(),
    val dataviewCache: MutableMap<Pair<DataviewField,DataviewValue>, MutableSet<Filename>> = mutableMapOf(),
    val backlinkCache: MutableMap<Filename, MutableSet<Filename>> = mutableMapOf()
) {
    fun addFileData(fileData: FileData, settings: MinionSettings) : Either<MinionError, StateAccumulator> = either {
        files[fileData.name] = fileData
        addTags(fileData.tags, fileData.name)
        addBacklinks(fileData.outLinks, fileData.name)
        addDataview(fileData.dataview, fileData.name)
        addTasks(fileData.tasks, fileData.dataview, settings)
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

    fun addDataview(dataview: Map<DataviewField, DataviewValue>, filename: Filename)
    : Either<MinionError, StateAccumulator> = either {
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

    fun addTasks(tasks: List<Task>, dataview: Map<DataviewField, DataviewValue>, settings: MinionSettings)
    : Either<MinionError, StateAccumulator> = either {
        logger.debug { "addTasks()" }
        this@StateAccumulator.tasks.addAll(tasks.maybeAddDataviewValues(settings, dataview).bind())
        this@StateAccumulator
    }

    fun toState(settings: MinionSettings, plugin: MinionPlugin) : Either<MinionError, State> = either {
        State(
            plugin,
            settings,
            None,
            tasks,
            files,
            tagCache,
            dataviewCache,
            dataviewCache.keys.mapToFieldCache().bind(),
            backlinkCache
        )
    }
}
