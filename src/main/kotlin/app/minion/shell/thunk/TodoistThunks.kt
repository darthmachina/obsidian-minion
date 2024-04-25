package app.minion.shell.thunk

import app.minion.core.store.Action
import app.minion.core.store.State
import io.kvision.redux.ActionCreator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mu.KotlinLogging

private val logger = KotlinLogging.logger("TodoistThunks")

const val TODOIST_SYNC_URL = "https://api.todoist.com/sync/v9/sync"

interface TodoistThunks { companion object {
    fun syncTodoistTasks(apiToken: String, syncToken: String) : ActionCreator<Action, State> {
        return { dispatch, _ ->
            CoroutineScope(Dispatchers.Unconfined).launch {
                val axiosConfig: AxiosConfigSettings = jso {
                    url = TODOIST_SYNC_URL
                    method = "GET"
                    headers = jso {
                        "Authorization" to apiToken
                    }
                    params = jso {
                        "sync_token" to syncToken
                        "resource_types" to """["projects"]"""
                    }
                }
                logger.debug { "Executing Todoist request" }
                axios<TodoistResponse>(axiosConfig)
                    .then { response ->
                        logger.debug { "Projects: ${response.data.projects}" }
                    }
            }
        }
    }
}}

data class TodoistResponse(
    val sync_token: String,
    val projects: List<TodoistResponseProject>,
    val items: List<TodoistResponseItem>
)

data class TodoistResponseProject(
    val id: String,
    val name: String,
    val color: String,
    val is_deleted: Boolean,
    val is_archived: Boolean
)

data class TodoistResponseItem(
    val id: String,
    val project_id: String,
    val content: String,
    val description: String,
    val due: dynamic,
    val priority: Int,
    val parent_id: String?,
    val child_order: Int,
    val labels: List<String>,
    val checked: Boolean,
    val is_deleted: Boolean,
    val duration: TodoistResponseDuration
)

data class TodoistResponseDuration(
    val amount: Int,
    val unit: String
)

@Suppress("NOTHING_TO_INLINE")
inline fun <T: Any> jso(): T = js("({})") as T

inline fun <T: Any> jso(init: T.() -> Unit): T = jso<T>().apply(init)
