package app.minion.core.functions

import app.minion.core.MinionError
import app.minion.core.functions.MarkdownConversionFunctions.Companion.completeAsMarkdown
import app.minion.core.functions.RepeatingTaskFunctions.Companion.maybeRepeat
import app.minion.core.model.Task
import app.minion.core.store.ReducerFunctions.Companion.replaceTask
import arrow.core.Either
import arrow.core.Option
import arrow.core.raise.either
import arrow.core.toOption
import kotlinx.datetime.Clock
import mu.KotlinLogging

private val logger = KotlinLogging.logger("TaskFunctions")

interface TaskFunctions { companion object {
    fun Task.complete(): Either<MinionError, Pair<Task, Option<Task>>> = either {
        logger.debug { "Task.complete()" }

        Pair(
            this@complete.copy(
                completedOn = Clock.System.now().toOption()
            ),
            this@complete.maybeRepeat()
        )
    }

    fun Task.completeSubtask(subtask: Task): Either<MinionError, Task> = either {
        logger.info { "completeSubtask()" }
        this@completeSubtask.copy(
            subtasks = this@completeSubtask.subtasks
                .replaceTask(
                    subtask.copy(
                        completedOn = Clock.System.now().toOption(),
                        fileInfo = subtask.fileInfo.copy(original = subtask.completeAsMarkdown())
                    )
                )
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
