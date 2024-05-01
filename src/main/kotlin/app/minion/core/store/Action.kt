package app.minion.core.store

import app.minion.core.MinionError
import app.minion.core.model.File
import app.minion.core.model.FileData
import app.minion.core.model.Filename
import app.minion.core.model.MinionSettings
import app.minion.core.model.PageTaskField
import app.minion.core.model.Task
import app.minion.core.model.todoist.Project
import app.minion.core.model.todoist.Section
import app.minion.core.model.todoist.TodoistTask
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
        val pageTaskFields: Option<List<PageTaskField>> = None,
        val todoistApiToken: Option<String> = None
    ) : Action
    data class LoadInitialState(val state: State) : Action
    data class LoadDataForFile(val fileData: FileData) : Action
    data class RemoveDataForFile(val name: Filename) : Action
    data class FileRenamed(val oldPath: File, val file: File) : Action
    data class TodoistUpdated(
        val syncToken: String,
        val updatedProjects: List<Project>,
        val updatedSections: List<Section>,
        val updatedTasks: List<TodoistTask>) : Action
}
