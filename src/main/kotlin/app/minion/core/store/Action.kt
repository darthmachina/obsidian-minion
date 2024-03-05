package app.minion.core.store

import io.kvision.redux.RAction

sealed interface Action : RAction {
    data class DisplayError(val error: String) : Action
    data class LoadInitialState(val state: State) : Action
}
