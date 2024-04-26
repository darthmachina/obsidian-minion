package app.minion.shell.thunk

import RequestUrlParam
import app.minion.core.MinionError
import app.minion.core.model.todoist.Priority
import app.minion.core.model.todoist.Project
import app.minion.core.model.todoist.TodoistTask
import app.minion.core.store.Action
import app.minion.core.store.State
import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.toOption
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
                    either {
                        val requestConfig: RequestUrlParam = jso {
                            url = """$TODOIST_SYNC_URL?sync_token=$syncToken&resource_types=["projects", "items"]"""
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
                            response.text
                        )
                        logger.debug { "sync_token: ${todoistData.sync_token}" }
                        logger.debug { "project count: ${todoistData.projects.size}" }
                        logger.debug { "items count: ${todoistData.items.size}" }

                        val projects = todoistData.projects.toModel().bind()
                        val items = todoistData.items.toModel(projects).bind()
                        logger.debug { "Dispatching TodoistUpdated" }
                        dispatch(Action.TodoistUpdated(todoistData.sync_token, projects, items))
                    }
                        .mapLeft {
                            logger.error { "Error getting Todoist data: $it" }
                        }
                } catch (ex: Exception) {
                    logger.error(ex) { "Error getting Todoist data: $ex" }
                }
            }
        }
    }
}}

fun List<TodoistResponseProject>.toModel() : Either<MinionError, List<Project>> = either {
    this@toModel
        .mapNotNull { responseProject ->
            if (responseProject.is_archived || responseProject.is_deleted) {
                null
            } else {
                Project(responseProject.id, responseProject.name, responseProject.color)
            }
        }
}

fun List<TodoistResponseItem>.toModel(projects: List<Project>) : Either<MinionError, List<TodoistTask>> = either {
    this@toModel
        .groupBy { it.id }
        .mapValues { it.value.first().toTask(projects).bind() }
        .let {
            val taskMap = it.toMutableMap()
            this@toModel
                .sortedBy { it.child_order }
                .forEach { responseTask ->
                    if (responseTask.parent_id != null) {
                        taskMap[responseTask.parent_id] = taskMap[responseTask.parent_id]!!.copy(
                            subtasks = taskMap[responseTask.parent_id]!!.subtasks.plus(taskMap[responseTask.id]!!)
                        )
                    }
                }
            taskMap.toMap()
                .entries
                .map { it.value }
                .filter { task ->
                    this@toModel
                        .find { it.id == task.id }
                        .toOption()
                        .map { it.parent_id == null }
                        .getOrElse { false }

                }
        }
}

fun TodoistResponseItem.toTask(projects: List<Project>) : Either<MinionError, TodoistTask> = either {
    TodoistTask(
        this@toTask.id,
        this@toTask.content,
        projects.findProjectById(this@toTask.project_id).bind(),
        this@toTask.description,
        this@toTask.due.string,
        this@toTask.priority.toPriority().bind(),
        this@toTask.labels
    )
}

fun List<Project>.findProjectById(id: String) : Either<MinionError, Project> = either {
    this@findProjectById
        .find { it.id == id }
        .toOption()
        .getOrElse {
            raise(MinionError.TodoistError("Cannot find Project with ID $id"))
        }
}

fun Int.toPriority() : Either<MinionError, Priority> = either {
    when (this@toPriority) {
        1 -> Priority.ONE
        2 -> Priority.TWO
        3 -> Priority.THREE
        4 -> Priority.FOUR
        else -> raise(MinionError.TodoistError("Priority $this@toPriority is not a valid Priority"))
    }
}

@Serializable
data class TodoistResponse(
    val sync_token: String,
    val projects: List<TodoistResponseProject>,
    val items: List<TodoistResponseItem>
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
