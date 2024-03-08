package app.minion.core.store

import app.minion.core.MinionError
import app.minion.core.model.FileData
import app.minion.core.model.Task
import io.kvision.redux.RAction

sealed interface Action : RAction {
    data class DisplayError(val error: MinionError) : Action
    data class LoadInitialState(val state: State) : Action
    data class LoadDataForFile(val fileData: FileData) : Action
    data class TaskCompleted(val task: Task) : Action
    data class SubtaskCompleted(val task: Task, val subtask: Task) : Action
    data class TaskUpdated(val task: Task) : Action
}
