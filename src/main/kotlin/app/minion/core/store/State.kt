package app.minion.core.store

import MinionPlugin
import app.minion.core.model.DataviewField
import app.minion.core.model.DataviewValue
import app.minion.core.model.FileData
import app.minion.core.model.Filename
import app.minion.core.model.Tag
import app.minion.core.model.Task
import io.kvision.redux.TypedReduxStore

typealias MinionStore = TypedReduxStore<State, Action>

data class State(
    val plugin: MinionPlugin,
    val tasks: List<Task>,
    val files: Map<Filename, FileData>,
    val tagCache: Map<Tag, List<Filename>>,
    val dataviewCache: Map<Pair<DataviewField, DataviewValue>, List<Filename>>
)
