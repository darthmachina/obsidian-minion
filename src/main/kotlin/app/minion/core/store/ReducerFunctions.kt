package app.minion.core.store

import app.minion.core.MinionError
import app.minion.core.model.FileData
import app.minion.core.model.Task
import app.minion.core.store.StateFunctions.Companion.addToBacklinkCache
import app.minion.core.store.StateFunctions.Companion.addToDataviewCache
import app.minion.core.store.StateFunctions.Companion.addToTagCache
import app.minion.core.store.StateFunctions.Companion.replaceData
import arrow.core.Either
import arrow.core.raise.either
import mu.KotlinLogging

private val logger = KotlinLogging.logger("ReducerFunctions")

interface ReducerFunctions { companion object {
    fun State.replaceDataForFile(fileData: FileData) : Either<MinionError, State> = either {
        this@replaceDataForFile.copy(
            files = this@replaceDataForFile.files.replaceData(fileData),
            tagCache = fileData.addToTagCache(this@replaceDataForFile.tagCache),
            dataviewCache = fileData.addToDataviewCache(this@replaceDataForFile.dataviewCache),
            backlinkCache = fileData.addToBacklinkCache(this@replaceDataForFile.backlinkCache)
        )
    }

    /**
     * Replaces a Task within the Task list, based on the Tasks UUID.
     *
     * @receiver List of Tasks to process
     * @param newTask The updated Task
     * @return List of Tasks with the Task with the same UUID replaced
     */
    fun List<Task>.replaceTask(newTask: Task) : List<Task> {
        logger.info { "replaceTask()" }
        return this.map {
            if (it.id == newTask.id) {
                newTask
            } else {
                it
            }
        }
    }
}}
