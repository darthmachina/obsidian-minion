package app.minion.core.functions

import app.minion.core.MinionError
import app.minion.core.functions.MarkdownConversionFunctions.Companion.completeAsMarkdown
import app.minion.core.model.Task
import app.minion.core.store.ReducerFunctions.Companion.replaceTask
import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.raise.either
import arrow.core.toOption
import kotlinx.datetime.Clock
import mu.KotlinLogging

private val logger = KotlinLogging.logger("TaskFunctions")

interface TaskFunctions { companion object {
    fun Task.complete() : Either<MinionError.TaskModificationError, Pair<Task, Option<Task>>> = either {
        logger.debug { "Task.complete()" }

        Pair(
            this@complete.copy(
                completedOn = Clock.System.now().toOption()
            ),
            None
        )
    }

    fun Task.completeSubtask(subtask: Task) : Either<MinionError.TaskModificationError, Task> = either {
        logger.info { "completeSubtask()" }
        this@completeSubtask.copy(subtasks = this@completeSubtask.subtasks
            .replaceTask(
                subtask.copy(
                    completedOn = Clock.System.now().toOption(),
                    fileInfo = subtask.fileInfo.copy(original = subtask.completeAsMarkdown())
                )
            )
        )
    }
}}
