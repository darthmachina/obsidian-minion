package app.minion.shell.thunk

import RequestUrlParam
import app.minion.core.store.Action
import app.minion.core.store.State
import io.kvision.redux.ActionCreator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import requestUrl

private val logger = KotlinLogging.logger("TodoistThunks")

const val TODOIST_SYNC_URL = "https://api.todoist.com/sync/v9/sync"

private val json = Json { ignoreUnknownKeys = true }

interface TodoistThunks { companion object {
    fun syncTodoistTasks(apiToken: String, syncToken: String) : ActionCreator<Action, State> {
        return { dispatch, _ ->
            CoroutineScope(Dispatchers.Unconfined).launch {
                try {
                    val requestConfig: RequestUrlParam = jso {
                        url = """$TODOIST_SYNC_URL?sync_token=$syncToken&resource_types=["projects"]"""
                        method = "GET"
                        headers = jso {
                            Authorization = "Bearer $apiToken"
                        }
                    }
                    logger.debug { "Executing Todoist request:\n\t${requestConfig.url}\n\t${requestConfig.headers}" }
                    val response = requestUrl(requestConfig).await()
                    logger.debug { "Response: ${response.text}" }
                    val todoistData = json.decodeFromString(
                        TodoistResponse.serializer(),
                        response.text)
                    logger.debug { "sync_token: ${todoistData.sync_token}" }
                    logger.debug { "project count: ${todoistData.projects.size}" }
                }catch (ex: Exception) {
                    logger.error(ex) { "Error connecting to Todoist: $ex" }
                }
            }
        }
    }
}}

@Serializable
data class TodoistResponse(
    val sync_token: String,
    val projects: List<TodoistResponseProject>,
//    val items: List<TodoistResponseItem>
)

@Serializable
data class TodoistResponseProject(
    val id: String,
    val name: String,
    val color: String,
    val is_deleted: Boolean,
    val is_archived: Boolean
)

@Serializable
data class TodoistResponseItem(
    val id: String,
    val project_id: String,
    val content: String,
    val description: String,
    val due: TodoistDueDate,
    val priority: Int,
    val parent_id: String?,
    val child_order: Int,
    val labels: List<String>,
    val checked: Boolean,
    val is_deleted: Boolean,
    val duration: TodoistResponseDuration
)

@Serializable
data class TodoistDueDate(
    val string: String,
    val date: String,
    val timezone: String,
    val lang: String,
    val is_recurring: Boolean
)

@Serializable
data class TodoistResponseDuration(
    val amount: Int,
    val unit: String
)

@Suppress("NOTHING_TO_INLINE")
inline fun <T: Any> jso(): T = js("({})") as T

inline fun <T: Any> jso(init: T.() -> Unit): T = jso<T>().apply(init)
