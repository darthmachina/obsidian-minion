package app.minion.core.store

import app.minion.core.MinionError
import app.minion.core.functions.SettingsFunctions.Companion.toJson
import app.minion.core.store.ReducerFunctions.Companion.fileRenamed
import app.minion.core.store.ReducerFunctions.Companion.removeDataForFile
import app.minion.core.store.ReducerFunctions.Companion.replaceDataForFile
import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.right
import arrow.core.toOption
import mu.KotlinLogging
import mu.KotlinLoggingConfiguration

private val logger = KotlinLogging.logger("Reducer")

fun reducer(state: State, action: Action) : State =
    when(action) {
        is Action.DisplayError -> { state.copy(error = action.error.toOption()) }
        is Action.LoadSettings -> {
            KotlinLoggingConfiguration.LOG_LEVEL = action.settings.logLevel
            state.copy(settings = action.settings)
        }
        is Action.UpdateSettings -> {
            logger.debug { "UpdateSettings : $action" }
            val newSettings = state.settings.copy(
                lifeAreas = action.lifeAreas.getOrElse { state.settings.lifeAreas },
                excludeFolders = action.excludeFolders.getOrElse { state.settings.excludeFolders },
                logLevel = action.logLevel.getOrElse { state.settings.logLevel },
                pageTaskFields = action.pageTaskFields.getOrElse { state.settings.pageTaskFields },
                todoistApiToken = action.todoistApiToken.getOrElse { state.settings.todoistApiToken }
            )
            logger.debug { "new Settings: $newSettings" }
            action.logLevel.map {
                KotlinLoggingConfiguration.LOG_LEVEL = it
            }
            state.plugin.saveData(newSettings.toJson())
            state.copy(settings = newSettings)
        }
        is Action.LoadInitialState -> {
            logger.debug { "LoadInitialState: ${action.state}" }
            action.state
        }
        is Action.LoadDataForFile -> handleError(state, action) { s, a ->
            logger.debug { "LoadDataForFile: ${a.fileData}" }
            val result = s.replaceDataForFile(a.fileData)
            logger.debug { " - updated state: $result" }
            result
        }
        is Action.RemoveDataForFile -> handleError(state, action) { s, a ->
            logger.debug { "RemoveDataForFile: ${a.name.v}" }
            s.removeDataForFile(a.name)
        }
        is Action.FileRenamed -> handleError(state, action) { s, a ->
            logger.debug { "FileRenamed: ${a.oldPath.v} to ${a.file.v}" }
            s.fileRenamed(a.file, a.oldPath)
        }
        is Action.TodoistUpdated -> handleError(state, action) { s, a ->
            logger.debug { "TodoistUpdated" }
            s.copy(
                todoistSyncToken = a.syncToken,
                projects = a.updatedProjects,
                sections = a.updatedSections,
                tasks = a.updatedTasks
            ).right()
        }
    }

inline fun <A> handleError(state: State, action: A, block: (State, A) -> Either<MinionError, State>) : State =
    block.invoke(state, action)
        .getOrElse { state.copy(error = it.toOption()) }
