package app.minion.shell.thunk

import app.minion.core.MinionError
import app.minion.core.store.Action
import app.minion.core.store.State
import arrow.core.Either
import arrow.core.raise.either
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.kvision.redux.ActionCreator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import mu.KotlinLogging

private val logger = KotlinLogging.logger("TodoistThunks")

const val TODOIST_BASE_URL = "https://api.todoist.com/sync/v9/"

interface TodoistThunks { companion object {
    fun syncTodoistTasks(client: HttpClient, syncToken: String) : ActionCreator<Action, State> {
        return { dispatch, _ ->
            CoroutineScope(Dispatchers.Unconfined).launch {
                client.executeTodoistRequest("sync", syncToken)
                    .map { response ->
                        val projectJson = response["projects"]
                        val taskJson = response["items"]
                        logger.debug { "Response:\t$projectJson\n$taskJson" }
                    }
            }
        }
    }

    suspend fun HttpClient.executeTodoistRequest(endpoint: String, syncToken: String) : Either<MinionError, Map<String, JsonElement>> = either {
        val response = this@executeTodoistRequest.get("$TODOIST_BASE_URL$endpoint") {
            headers {
                append(HttpHeaders.Authorization, "")
            }
            url {
                parameters.append("sync_token", syncToken)
                parameters.append("resource_types", """["projects", "items"]""")
            }
        }
        if (response.status.value !in 200..299) {
            raise(MinionError.TodoistError("Error connecting to Todoist\n$response"))
        }
        Json.parseToJsonElement(response.body()).jsonObject.toMap()
    }
}}
