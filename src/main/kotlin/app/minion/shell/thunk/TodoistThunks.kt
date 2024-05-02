package app.minion.shell.thunk

import Notice
import RequestUrlParam
import app.minion.core.MinionError
import app.minion.core.functions.DateTimeFunctions
import app.minion.core.model.Content
import app.minion.core.model.DateTime
import app.minion.core.model.Tag
import app.minion.core.model.todoist.Priority
import app.minion.core.model.todoist.Project
import app.minion.core.model.todoist.Section
import app.minion.core.model.todoist.TodoistTask
import app.minion.core.store.Action
import app.minion.core.store.State
import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.some
import arrow.core.toOption
import io.kvision.redux.ActionCreator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.uuid.UUID
import mu.KotlinLogging
import requestUrl

private val logger = KotlinLogging.logger("TodoistThunks")

const val TODOIST_SYNC_URL = "https://api.todoist.com/sync/v9/sync"

private val json = Json { ignoreUnknownKeys = true }

interface TodoistThunks { companion object {
    fun syncTodoistTasks(apiToken: String, syncToken: String) : ActionCreator<Action, State> {
        return { dispatch, state ->
            CoroutineScope(Dispatchers.Unconfined).launch {
                try {
                    either {
                        val requestConfig: RequestUrlParam = jso {
                            url = """$TODOIST_SYNC_URL?sync_token=$syncToken&resource_types=["projects", "items", "sections"]"""
                            method = "POST"
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

                        val currentState = state()

                        val projects = todoistData.projects.toModel(currentState.projects).bind()
                        val sections = todoistData.sections.toModel(currentState.sections).bind()
                        val items = todoistData.items.toModel(projects, sections, currentState.tasks).bind()

                        logger.debug { "Dispatching TodoistUpdated" }
                        if (syncToken != "*") {
                            logger.debug { "Updated items: ${todoistData.items}" }
                        }
                        dispatch(Action.TodoistUpdated(todoistData.sync_token, projects, sections, items))
                        Notice("Todoist pull completed")
                    }
                        .mapLeft {
                            logger.error { "Error getting Todoist data: $it" }
                            Notice("Minion: $it")
                        }
                } catch (ex: Exception) {
                    logger.error(ex) { "Error getting Todoist data: $ex" }
                    Notice("Minion: $ex")
                }
            }
        }
    }

    fun completeTask(task: TodoistTask, apiToken: String) : ActionCreator<Action, State> {
        return { _, _ ->
            logger.debug { "TodoistThunks.complete()" }
            CoroutineScope(Dispatchers.Unconfined).launch {
                val requestConfig: RequestUrlParam = jso {
                    url =
                        """$TODOIST_SYNC_URL?commands=[{"type":"item_close", "uuid": "${UUID()}", "args": {"id": "${task.id}"}}]"""
                    method = "POST"
                    headers = jso {
                        Authorization = "Bearer $apiToken"
                    }
                    throws = false
                }
                logger.debug { "Executing Todoist request:\n\t${requestConfig.url}\n\t${requestConfig.headers}" }
                val response = requestUrl(requestConfig).await()
                if (response.status != 200) {
                    logger.debug { "Response: ${response.text}" }
                }
                Notice("Task completed (${response.status})")
            }
        }
    }

    fun addToProject(content: Content, project: Project, apiToken: String) : ActionCreator<Action, State> {
        return { _, _ ->
            logger.debug { "TodoistThunks.addToProject()" }
            CoroutineScope(Dispatchers.Unconfined).launch {
                val requestConfig: RequestUrlParam = jso {
                    url =
                        """$TODOIST_SYNC_URL?commands=[{"type":"item_add", "temp_id": "${UUID()}",  "uuid": "${UUID()}", "args": {"content": "${content.v}", "project_id": "${project.id}"}}]"""
                    method = "POST"
                    headers = jso {
                        Authorization = "Bearer $apiToken"
                    }
                    throws = false
                }
                logger.debug { "Executing Todoist request:\n\t${requestConfig.url}\n\t${requestConfig.headers}" }
                val response = requestUrl(requestConfig).await()
                logger.debug { "Response: ${response.text}" }
                Notice("Task added (${response.status})")
            }
        }
    }
}}

fun List<TodoistResponseProject>.toModel(existingProjects: List<Project>)
: Either<MinionError, List<Project>> = either {
    this@toModel
        .mapNotNull { responseProject ->
            if (responseProject.is_archived || responseProject.is_deleted) {
                null
            } else {
                Project(responseProject.id, responseProject.name, responseProject.color)
            }
        }
        .let { projects ->
            val incomingIds = projects.map { it.id }
            existingProjects
                .filter { existingProject ->
                    !incomingIds.contains(existingProject.id)
                }
                .plus(projects)
        }
}

fun List<TodoistResponseSection>.toModel(existingSections: List<Section>)
: Either<MinionError, List<Section>> = either {
    val incomingIds = this@toModel.map { it.id }
    existingSections
        .filter { !incomingIds.contains(it.id) }
        .plus(
            this@toModel
                .filter { !it.is_deleted && !it.is_archived }
                .map { Section(it.id, it.name) }
        )
}

fun List<TodoistResponseItem>.toModel(
    projects: List<Project>,
    sections: List<Section>,
    existingTasks: List<TodoistTask>)
: Either<MinionError, List<TodoistTask>> = either {
    val incomingIds = this@toModel.map { it.id }
    val sectionMap = sections.groupBy { it.id }.mapValues { it.value.first() }
    existingTasks
        .filter { !incomingIds.contains(it.id) }
        .plus(
            this@toModel
                .filter { !it.is_deleted && !it.checked }
                .map { it.toTask(projects, sectionMap).bind() }
        )
}

fun TodoistResponseItem.toTask(projects: List<Project>, sections: Map<String, Section>)
: Either<MinionError, TodoistTask> = either {
    TodoistTask(
        this@toTask.id,
        Content(this@toTask.content),
        projects.findProjectById(this@toTask.project_id).bind(),
        this@toTask.description,
        sections[this@toTask.section_id]?.some() ?: None,
        this@toTask.due.toDateTime().bind(),
        this@toTask.priority.toPriority().bind(),
        this@toTask.labels.map { Tag(it) }.toSet(),
        this@toTask.parent_id?.some() ?: None
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

fun TodoistDueDate?.toDateTime() : Either<MinionError, Option<DateTime>> = either {
    if (this@toDateTime == null) {
        None
    } else {
        if (this@toDateTime.timezone == null) {
            // Process as current timezone
            DateTimeFunctions.parseDateTime(this@toDateTime.date).bind().some()
        } else {
            // Process as GMT timezone
            Instant
                .parse(this@toDateTime.date)
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .let {
                    DateTime(it.date, it.time.some()).some()
                }
        }
    }
}

@Serializable
data class TodoistResponse(
    val sync_token: String,
    val projects: List<TodoistResponseProject>,
    val sections: List<TodoistResponseSection>,
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
data class TodoistResponseSection(
    val id: String,
    val name: String,
    val is_archived: Boolean,
    val is_deleted: Boolean
)

@Serializable
data class TodoistResponseItem(
    val id: String,
    val project_id: String,
    val content: String,
    val description: String,
    val due: TodoistDueDate?,
    val priority: Int,
    val parent_id: String?,
    val section_id: String?,
    val child_order: Int,
    val labels: List<String>,
    val checked: Boolean,
    val is_deleted: Boolean,
    val duration: TodoistResponseDuration?,
    val subItems: List<TodoistResponseItem> = emptyList()
)

@Serializable
data class TodoistDueDate(
    val string: String,
    val date: String,
    val timezone: String?,
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
