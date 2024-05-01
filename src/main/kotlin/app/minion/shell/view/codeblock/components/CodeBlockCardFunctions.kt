package app.minion.shell.view.codeblock.components

import app.minion.core.MinionError
import app.minion.core.model.DataviewField
import app.minion.core.model.FileData
import app.minion.core.model.Task
import app.minion.core.model.todoist.TodoistTask
import app.minion.core.store.MinionStore
import app.minion.shell.thunk.TodoistThunks
import app.minion.shell.view.ViewFunctions.Companion.getWikilinkResourcePath
import app.minion.shell.view.ViewFunctions.Companion.outputStyledContent
import app.minion.shell.view.codeblock.CodeBlockConfig
import app.minion.shell.view.ICON_GROUP
import app.minion.shell.view.ICON_KANBAN
import app.minion.shell.view.ICON_MENU
import app.minion.shell.view.Item
import app.minion.shell.view.PropertyType
import app.minion.shell.view.ViewModelFunctions.Companion.getPropertyValue
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
    fun Item.getImagePath(store: MinionStore) : Either<MinionError, String> = either {
        this@getImagePath
            .getPropertyValue(PropertyType.IMAGE)
            .map {
                it.getWikilinkResourcePath(
                    store.store.state.plugin.app.vault,
                    store.store.state.plugin.app.metadataCache
                ).bind()
            }.bind()
    }

    fun FlowContent.outputSubtasks(item: Item, store: MinionStore) {
        item.todoist.map { task ->
            task.subtasks.forEach { subtask ->
                    div(classes = "mi-codeblock-task-subtask") {
                        checkBoxInput {
                            onClickFunction = {
                                store.dispatch(
                                    (TodoistThunks.completeTask(
                                        subtask,
                                        store.store.state.settings.todoistApiToken
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

    fun FlowContent.createChangeKanbanMenuItem(task: TodoistTask, config: CodeBlockConfig, store: MinionStore)
    : FlowContent.() -> Unit {
        return {
            a {
                title = "Change kanban status"
                unsafe { +ICON_KANBAN }
                onClickFunction = {
                    //KanbanStatusSelectModal(store, task, store.store.state.plugin.app).open()
                }
            }
        }
    }
}}
