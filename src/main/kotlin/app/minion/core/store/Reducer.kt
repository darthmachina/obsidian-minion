package app.minion.core.store

import mu.KotlinLogging

private val logger = KotlinLogging.logger {  }

fun reducer(state: State, action: Action) : State =
    when(action) {
        is Action.DisplayError -> { state }
        is Action.LoadInitialState -> {
            logger.debug { "LoadInitialState: ${action.state}" }
            action.state
        }
    }
