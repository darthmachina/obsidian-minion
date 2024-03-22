package app.minion.core.store

import app.minion.core.MinionError
import app.minion.core.functions.FileDataFunctions.Companion.addPageTags
import app.minion.core.functions.TaskFunctions.Companion.replaceTask
import app.minion.core.model.FileData
import app.minion.core.model.Task
import app.minion.core.store.StateFunctions.Companion.updateBacklinkCache
import app.minion.core.store.StateFunctions.Companion.updateDataviewCache
import app.minion.core.store.StateFunctions.Companion.updateTagCache
import app.minion.core.store.StateFunctions.Companion.replaceData
import app.minion.core.store.StateFunctions.Companion.replaceTasks
import arrow.core.Either
import arrow.core.None
import arrow.core.raise.either
import mu.KotlinLogging

private val logger = KotlinLogging.logger("ReducerFunctions")

interface ReducerFunctions { companion object {
    fun State.replaceDataForFile(fileData: FileData) : Either<MinionError, State> = either {
        this@replaceDataForFile.copy(
            files = this@replaceDataForFile.files.replaceData(fileData),
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

    fun State.replaceTask(newTask: Task) : Either<MinionError, State> = either {
        this@replaceTask.copy(
            tasks = this@replaceTask.tasks.replaceTask(newTask),
            error = None
        )
    }
}}
