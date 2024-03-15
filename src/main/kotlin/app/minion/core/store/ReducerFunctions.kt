package app.minion.core.store

import app.minion.core.MinionError
import app.minion.core.functions.TaskFunctions.Companion.replaceTask
import app.minion.core.model.DataviewField
import app.minion.core.model.DataviewValue
import app.minion.core.model.FileData
import app.minion.core.model.Filename
import app.minion.core.model.MinionSettings
import app.minion.core.model.Tag
import app.minion.core.model.Task
import app.minion.core.store.StateFunctions.Companion.addToBacklinkCache
import app.minion.core.store.StateFunctions.Companion.addToDataviewCache
import app.minion.core.store.StateFunctions.Companion.addToTagCache
import app.minion.core.store.StateFunctions.Companion.replaceData
import app.minion.core.store.StateFunctions.Companion.replaceTasks
import arrow.core.Either
import arrow.core.None
import arrow.core.mapNotNull
import arrow.core.raise.either
import mu.KotlinLogging

private val logger = KotlinLogging.logger("ReducerFunctions")

interface ReducerFunctions { companion object {
    fun State.replaceDataForFile(fileData: FileData) : Either<MinionError, State> = either {
        this@replaceDataForFile.copy(
            files = this@replaceDataForFile.files.replaceData(fileData),
            tagCache = fileData.addToTagCache(this@replaceDataForFile.tagCache),
            dataviewCache = fileData.addToDataviewCache(this@replaceDataForFile.dataviewCache),
            backlinkCache = fileData.addToBacklinkCache(this@replaceDataForFile.backlinkCache),
            tasks = fileData.replaceTasks(this@replaceDataForFile.tasks),
            error = None
        )
    }

    fun State.replaceTask(newTask: Task) : Either<MinionError, State> = either {
        this@replaceTask.copy(
            tasks = this@replaceTask.tasks.replaceTask(newTask),
            error = None
        )
    }

    fun State.updateForExcludedFolders() : Either<MinionError, State> = either {
        val excludedFilenames = this@updateForExcludedFolders
            .files
            .filenamesForExcludedFolders(this@updateForExcludedFolders.settings)
            .bind()
        this@updateForExcludedFolders.copy(
            tasks = this@updateForExcludedFolders.tasks.filterExcludedFolders(this@updateForExcludedFolders.settings).bind(),
            files = this@updateForExcludedFolders.files.filter { excludedFilenames.contains(it.key) },
            tagCache = this@updateForExcludedFolders.tagCache.filterExcludedFolders(excludedFilenames).bind(),
            dataviewCache = this@updateForExcludedFolders.dataviewCache.filterExcludedFolders(excludedFilenames).bind(),
            backlinkCache = this@updateForExcludedFolders.backlinkCache.filterExcludedFolders(excludedFilenames).bind()
        )
    }

    fun List<Task>.filterExcludedFolders(settings: MinionSettings) : Either<MinionError, List<Task>> = either {
        this@filterExcludedFolders
            .filter { task ->
                !settings.excludeFolders.any { task.fileInfo.path.v.startsWith(it) }
            }
    }

    fun Map<Filename, FileData>.filenamesForExcludedFolders(settings: MinionSettings) : Either<MinionError, Set<Filename>> = either {
        this@filenamesForExcludedFolders
            .mapValues { entry -> settings.excludeFolders.any { entry.value.path.v.startsWith(it) } }
            .mapNotNull { if (it.value) it.key else null }
            .map { it.value }
            .toSet()
    }

    fun Map<Tag, Set<Filename>>.filterExcludedFolders(files: Set<Filename>) : Either<MinionError, Map<Tag, Set<Filename>>> = either {
        this@filterExcludedFolders
            .mapValues { entry ->
                entry.value.filter { !files.contains(it) }.toSet()
            }
    }

    fun Map<Pair<DataviewField, DataviewValue>, Set<Filename>>.filterExcludedFolders(files: Set<Filename>) : Either<MinionError, Map<Pair<DataviewField, DataviewValue>, Set<Filename>>> = either {
        this@filterExcludedFolders
            .mapValues { entry ->
                entry.value.filter { !files.contains(it) }.toSet()
            }
    }

    fun Map<Filename, Set<Filename>>.filterExcludedFolders(files: Set<Filename>) : Either<MinionError, Map<Filename, Set<Filename>>> = either {
        this@filterExcludedFolders
            .mapValues { entry ->
                entry.value.filter { !files.contains(it) }.toSet()
            }
    }
}}
