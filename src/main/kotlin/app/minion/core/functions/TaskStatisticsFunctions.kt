package app.minion.core.functions

import app.minion.core.MinionError
import app.minion.core.model.Task
import arrow.core.Either
import arrow.core.raise.either

interface TaskStatisticsFunctions { companion object {
    fun Task.completedSubtaskPercent() : Either<MinionError, Int> = either {
        if (this@completedSubtaskPercent.subtasks.isNotEmpty()) {
            this@completedSubtaskPercent.completedSubtaskCount() * 100 / this@completedSubtaskPercent.subtasks.size
        } else {
            raise(MinionError.NoSubtasksError("Task does have any subtasks to compute percentage"))
        }
    }

    fun Task.completedSubtaskCount() : Int {
        return this.subtasks.fold(0) { total, subtask ->
            if (subtask.completed) total + 1 else total
        }
    }
}}
