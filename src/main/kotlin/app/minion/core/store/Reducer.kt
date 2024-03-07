package app.minion.core.store

import app.minion.core.MinionError
import app.minion.core.store.ReducerFunctions.Companion.replaceDataForFile
import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.toOption
import mu.KotlinLogging

private val logger = KotlinLogging.logger {  }

fun reducer(state: State, action: Action) : State =
    when(action) {
        is Action.DisplayError -> { state }
        is Action.LoadInitialState -> {
            logger.debug { "LoadInitialState: ${action.state}" }
            action.state
        }
        is Action.LoadDataForFile -> handleError(state, action) { _, a ->
            logger.debug { "LoadDataForFile: ${a.fileData}" }
            val result = state.replaceDataForFile(a.fileData)
            logger.debug { " - updated state: $result" }
            result
        }
    }

inline fun <A> handleError(state: State, action: A, block: (State, A) -> Either<MinionError, State>) : State =
    block.invoke(state, action)
        .getOrElse { state.copy(error = it.toOption()) }
