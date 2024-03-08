package app.minion.core.store

import app.minion.core.MinionError
import app.minion.core.functions.TaskFunctions.Companion.replaceTask
import app.minion.core.model.FileData
import app.minion.core.model.Task
import app.minion.core.store.StateFunctions.Companion.addToBacklinkCache
import app.minion.core.store.StateFunctions.Companion.addToDataviewCache
import app.minion.core.store.StateFunctions.Companion.addToTagCache
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
}}
