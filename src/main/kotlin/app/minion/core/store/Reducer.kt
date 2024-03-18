package app.minion.core.store

import app.minion.core.MinionError
import app.minion.core.functions.SettingsFunctions.Companion.toJson
import app.minion.core.store.ReducerFunctions.Companion.replaceDataForFile
import app.minion.core.store.ReducerFunctions.Companion.replaceTask
import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.toOption
import mu.KotlinLogging
import mu.KotlinLoggingConfiguration

private val logger = KotlinLogging.logger {  }

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
                pageTaskFields = action.pageTaskFields.getOrElse { state.settings.pageTaskFields }
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
        is Action.TaskCompleted -> handleError(state, action) { s, a ->
            logger.debug { "TaskCompleted: ${a.task}" }
            s.replaceTask(a.task)
        }
        is Action.SubtaskCompleted -> handleError(state, action) { s, a ->
            logger.debug { "SubtaskCompleted: ${a.task}" }
            s.replaceTask(a.task)
        }
        is Action.TaskUpdated -> handleError(state, action) { s, a ->
            logger.debug { "TaskUpdated: ${a.task}" }
            s.replaceTask(a.task)
        }
    }

inline fun <A> handleError(state: State, action: A, block: (State, A) -> Either<MinionError, State>) : State =
    block.invoke(state, action)
        .getOrElse { state.copy(error = it.toOption()) }
