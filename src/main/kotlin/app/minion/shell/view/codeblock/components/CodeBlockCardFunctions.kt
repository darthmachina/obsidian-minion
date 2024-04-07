package app.minion.shell.view.codeblock.components

import app.minion.core.MinionError
import app.minion.core.model.DataviewField
import app.minion.core.model.FileData
import app.minion.core.model.Task
import app.minion.core.store.MinionStore
import app.minion.shell.thunk.TaskThunks
import app.minion.shell.view.ViewFunctions.Companion.getWikilinkResourcePath
import app.minion.shell.view.ViewFunctions.Companion.outputStyledContent
import app.minion.shell.view.codeblock.CodeBlockConfig
import app.minion.shell.view.codeblock.FIELD_IMAGE
import app.minion.shell.view.ICON_GROUP
import app.minion.shell.view.ICON_KANBAN
import app.minion.shell.view.ICON_MENU
import app.minion.shell.view.modal.KanbanStatusSelectModal
import app.minion.shell.view.modal.UpdateDataviewValue
import arrow.core.Either
import arrow.core.raise.either
import arrow.core.toOption
import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.checkBoxInput
import kotlinx.html.div
import kotlinx.html.js.onClickFunction
import kotlinx.html.span
import kotlinx.html.title
import kotlinx.html.unsafe

interface CodeBlockCardFunctions { companion object {
    fun FileData.getImagePath(store: MinionStore) : Either<MinionError, String> = either {
        this@getImagePath.dataview[DataviewField(FIELD_IMAGE)]
            .toOption()
            .toEither { MinionError.ImageNotFoundError("No image specified for ${this@getImagePath.name.v}") }
            .map {
                it.v
                    .getWikilinkResourcePath(
                        store.store.state.plugin.app.vault,
                        store.store.state.plugin.app.metadataCache
                    )
                    .bind()
            }.bind()
    }

    fun FlowContent.outputSubtasks(task: Task, store: MinionStore) {
        task.subtasks.forEach { subtask ->
            if (!subtask.completed) {
                div(classes = "mi-codeblock-task-subtask") {
                    checkBoxInput {
                        onClickFunction = {
                            store.dispatch(
                                (TaskThunks.completeSubtask(
                                    store.store.state.plugin.app,
                                    task,
                                    subtask
                                ))
                            )
                        }
                    }
                    span(classes = "mi-codeblock-task-subtask-content") {
                        outputStyledContent(subtask.content, store)
                    }
                }
            }
        }
    }

    fun FlowContent.outputCardMenu(menuItems: List<FlowContent.() -> Unit> ) {
        div(classes = "mi-codeblock-menu-container") {
            span(classes = "mi-icon mi-button") {
                unsafe { +ICON_MENU }
            }
            div(classes = "mi-codeblock-menu") {
                menuItems.forEach { item ->
                    item()
                }
            }
        }
    }

    fun FlowContent.createChangeGroupMenuItem(fileData: FileData, config: CodeBlockConfig, store: MinionStore)
    : FlowContent.() -> Unit {
        return {
            a {
                title = "Change group value"
                unsafe { +ICON_GROUP }
                onClickFunction = {
                    UpdateDataviewValue(
                        fileData,
                        config.groupByField,
                        fileData.dataview[DataviewField(config.groupByField)].toOption(),
                        store.store.state.dataviewValueCache[DataviewField(config.groupByField)]!!,
                        store,
                        store.store.state.plugin.app
                    ).open()
                }
            }
        }
    }

    fun FlowContent.createChangeKanbanMenuItem(task: Task, config: CodeBlockConfig, store: MinionStore)
    : FlowContent.() -> Unit {
        return {
            a {
                title = "Change kanban status"
                unsafe { +ICON_KANBAN }
                onClickFunction = {
                    KanbanStatusSelectModal(store, task, store.store.state.plugin.app).open()
                }
            }
        }
    }
}}
