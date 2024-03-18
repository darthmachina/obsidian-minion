package app.minion.core.store

import app.minion.core.MinionError
import app.minion.core.model.FileData
import app.minion.core.model.MinionSettings
import app.minion.core.model.PageTaskField
import app.minion.core.model.Task
import arrow.core.None
import arrow.core.Option
import io.kvision.core.Color
import io.kvision.redux.RAction
import mu.KotlinLoggingLevel

sealed interface Action : RAction {
    data class DisplayError(val error: MinionError) : Action
    data class LoadSettings(val settings: MinionSettings) : Action
    data class UpdateSettings(
        val lifeAreas: Option<Map<String, Color>> = None,
        val excludeFolders: Option<Set<String>> = None,
        val logLevel: Option<KotlinLoggingLevel> = None,
        val pageTaskFields: Option<List<PageTaskField>> = None
    ) : Action
    data class LoadInitialState(val state: State) : Action
    data class LoadDataForFile(val fileData: FileData) : Action
    data class TaskCompleted(val task: Task) : Action
    data class SubtaskCompleted(val task: Task, val subtask: Task) : Action
    data class TaskUpdated(val task: Task) : Action
}
