package app.minion.core.store

import app.minion.core.MinionError
import app.minion.core.model.FileData
import app.minion.core.model.Task
import arrow.core.Either
import arrow.core.raise.either
import mu.KotlinLogging

private val logger = KotlinLogging.logger("ReducerFunctions")

interface ReducerFunctions { companion object {
    fun State.replaceDataForFile(fileData: FileData) : Either<MinionError, State> = either {


        this@replaceDataForFile
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
