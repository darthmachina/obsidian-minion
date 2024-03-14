package app.minion.shell.thunk

import App
import TFile
import app.minion.core.functions.MarkdownConversionFunctions.Companion.completeAsMarkdown
import app.minion.core.functions.MarkdownConversionFunctions.Companion.toMarkdown
import app.minion.core.functions.TaskFunctions.Companion.complete
import app.minion.core.functions.TaskFunctions.Companion.completeSubtask
import app.minion.core.functions.TaskTagFunctions.Companion.addTag
import app.minion.core.functions.TaskTagFunctions.Companion.findTagWithPrefix
import app.minion.core.functions.TaskTagFunctions.Companion.replaceTag
import app.minion.core.model.Tag
import app.minion.core.model.Task
import app.minion.core.store.Action
import app.minion.core.store.State
import app.minion.shell.functions.VaultWriteFunctions.Companion.writeLine
import arrow.core.flatMap
import io.kvision.redux.ActionCreator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mu.KotlinLogging

private val logger = KotlinLogging.logger("TaskThunks")

interface TaskThunks { companion object {
    fun completeTask(app: App, task: Task) : ActionCreator<Action, State> {
        logger.info { "completeTask()" }
        return { dispatch, _ ->
            CoroutineScope(Dispatchers.Unconfined).launch {
                task.complete()
                    .map { completedPair ->
                        logger.info { "post complete() map: ${completedPair.first.fileInfo.file}" }
                        logger.info { "${app.metadataCache.getFirstLinkpathDest(completedPair.first.fileInfo.file.v, "")!!::class}" }
                        (app.metadataCache.getFirstLinkpathDest(completedPair.first.fileInfo.file.v, "") as TFile)
                            .writeLine(app.vault, completedPair.toMarkdown(), completedPair.first.fileInfo.line)
                        logger.info { "dispath(TaskComplete)" }
                        dispatch(Action.TaskCompleted(completedPair.first))
                    }
                    .mapLeft {
                        dispatch(Action.DisplayError(it))
                    }

            }
        }
    }

    fun completeSubtask(app: App, task: Task, subtask: Task) : ActionCreator<Action, State> {
        logger.info { "completeSubtask()" }
        return { dispatch, _ ->
            CoroutineScope(Dispatchers.Unconfined).launch {
                task.completeSubtask(subtask)
                    .map {
                        (app.metadataCache.getFirstLinkpathDest(task.fileInfo.file.v, "") as TFile)
                            .writeLine(app.vault, subtask.completeAsMarkdown(), subtask.fileInfo.line)
                        dispatch(Action.SubtaskCompleted(task, subtask))
                    }
            }
        }
    }

    /**
     * Replaces the tag with a #kanban/ prefix and replaces the subtag with the updated one
     */
    fun changeKanbanStatus(app: App, task: Task, updatedStatus: String) : ActionCreator<Action, State> {
        logger.debug { "changeKanbanStatus(${updatedStatus}" }
        return { dispatch, _ ->
            CoroutineScope(Dispatchers.Unconfined).launch {
                task
                    .findTagWithPrefix("kanban/")
                    .flatMap { task.replaceTag(it, Tag("kanban/$updatedStatus")) }
                    .map { task ->
                        (app.metadataCache.getFirstLinkpathDest(task.fileInfo.file.v, "") as TFile)
                            .writeLine(app.vault, task.toMarkdown(), task.fileInfo.line)
                        dispatch(Action.TaskUpdated(task))
                    }
                    .mapLeft {
                        logger.debug { "Tag not found, adding the new one" }
                        task
                            .addTag(Tag("kanban/$updatedStatus"))
                            .map { task ->
                                (app.metadataCache.getFirstLinkpathDest(task.fileInfo.file.v, "") as TFile)
                                    .writeLine(app.vault, task.toMarkdown(), task.fileInfo.line)
                                dispatch(Action.TaskUpdated(task))
                            }
                            .mapLeft {
                                dispatch(Action.DisplayError(it))
                            }
                    }
            }
        }
    }
}}
