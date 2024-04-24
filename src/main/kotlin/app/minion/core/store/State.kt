package app.minion.core.store

import MinionPlugin
import app.minion.core.MinionError
import app.minion.core.model.*
import app.minion.core.model.todoist.Project
import app.minion.core.model.todoist.TodoistTask
import arrow.core.Option
import io.ktor.client.*
import io.kvision.redux.TypedReduxStore

typealias MinionStore = TypedReduxStore<State, Action>

data class State(
    val plugin: MinionPlugin,
    val settings: MinionSettings,
    val error: Option<MinionError>,
    val files: Map<Filename, FileData>,
    val tagCache: Map<Tag, Set<Filename>>,
    val dataviewCache: Map<Pair<DataviewField, DataviewValue>, Set<Filename>>,
    val dataviewValueCache: Map<DataviewField, Set<DataviewValue>>,
    val backlinkCache: Map<Filename, Set<Filename>>,
    val todoistClient: HttpClient,
    val todoistSyncToken: String,
    val projects: List<Project>,
    val tasks: List<TodoistTask>
)
