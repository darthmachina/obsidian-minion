package app.minion.core.store

import MinionPlugin
import app.minion.core.MinionError
import app.minion.core.model.DataviewField
import app.minion.core.model.DataviewValue
import app.minion.core.model.FileData
import app.minion.core.model.Filename
import app.minion.core.model.Tag
import app.minion.core.model.Task
import arrow.core.Option
import io.kvision.redux.TypedReduxStore

typealias MinionStore = TypedReduxStore<State, Action>

data class State(
    val plugin: MinionPlugin,
    val error: Option<MinionError>,
    val tasks: List<Task>,
    val files: Map<Filename, FileData>,
    val tagCache: Map<Tag, Set<Filename>>,
    val dataviewCache: Map<Pair<DataviewField, DataviewValue>, Set<Filename>>,
    val backlinkCache: Map<Filename, Set<Filename>>
)
