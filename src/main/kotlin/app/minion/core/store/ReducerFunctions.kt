package app.minion.core.store

import app.minion.core.MinionError
import app.minion.core.functions.FileDataFunctions.Companion.addPageTags
import app.minion.core.functions.FileDataFunctions.Companion.parseFilename
import app.minion.core.functions.TaskFunctions.Companion.replaceTask
import app.minion.core.model.File
import app.minion.core.model.FileData
import app.minion.core.model.Filename
import app.minion.core.model.Task
import app.minion.core.store.StateFunctions.Companion.removeFor
import app.minion.core.store.StateFunctions.Companion.updateBacklinkCache
import app.minion.core.store.StateFunctions.Companion.updateDataviewCache
import app.minion.core.store.StateFunctions.Companion.updateTagCache
import app.minion.core.store.StateFunctions.Companion.upsertData
import app.minion.core.store.StateFunctions.Companion.replaceTasks
import arrow.core.Either
import arrow.core.None
import arrow.core.raise.either
import arrow.core.toOption
import mu.KotlinLogging

private val logger = KotlinLogging.logger("ReducerFunctions")

interface ReducerFunctions { companion object {
    fun State.replaceDataForFile(fileData: FileData) : Either<MinionError, State> = either {
        this@replaceDataForFile.copy(
            files = this@replaceDataForFile.files.upsertData(fileData),
            tagCache = fileData.updateTagCache(this@replaceDataForFile.tagCache),
            dataviewCache = fileData.updateDataviewCache(this@replaceDataForFile.dataviewCache),
            backlinkCache = fileData.updateBacklinkCache(this@replaceDataForFile.backlinkCache),
            tasks = this@replaceDataForFile
                .tasks
                .replaceTasks(
                    fileData
                        .addPageTags(this@replaceDataForFile.settings, fileData.dataview).bind()
                ),
            error = None
        )
    }

    fun State.removeDataForFile(name: Filename) : Either<MinionError, State> = either {
        this@removeDataForFile.copy(
            files = this@removeDataForFile.files.minus(name),
            tagCache = this@removeDataForFile.tagCache.removeFor(name),
            dataviewCache = this@removeDataForFile.dataviewCache.removeFor(name),
            backlinkCache = this@removeDataForFile.backlinkCache.removeFor(name),
            tasks = this@removeDataForFile.tasks.removeFor(name),
            error = None
        )
    }

    fun State.fileRenamed(newPath: File, oldPath: File) : Either<MinionError, State> = either {
        // Find filename from oldPath
        // Find FileData from filename
        // Remove data for FileData
        // Change Filename and File for FileData
        // Insert data for new FileData
        if (this@fileRenamed.settings.excludeFolders.any { oldPath.v.startsWith(it)}) {
            // Excluded folder so nothing to be done
            this@fileRenamed
        } else {
            val oldFilename = oldPath.parseFilename().bind()

            this@fileRenamed
                .files[oldFilename]
                .toOption()
                .toEither {
                    MinionError.FileNotFoundError("${oldPath.v} not found in state")
                }
                .map { oldFileData ->
                    oldFileData
                        .copy(
                            name = newPath.parseFilename().bind(),
                            path = newPath
                        )
                        .let { newFileData ->
                            this@fileRenamed
                                .removeDataForFile(oldFilename).bind()
                                .replaceDataForFile(newFileData).bind()
                        }
                }.bind()
        }
    }

    fun State.replaceTask(newTask: Task) : Either<MinionError, State> = either {
        this@replaceTask.copy(
            tasks = this@replaceTask.tasks.replaceTask(newTask),
            error = None
        )
    }
}}
