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
import app.minion.shell.view.Item
import app.minion.shell.view.PropertyType
import app.minion.shell.view.ViewModelFunctions.Companion.getPropertyValue
import app.minion.shell.view.modal.KanbanStatusSelectModal
import app.minion.shell.view.modal.UpdateDataviewValue
import arrow.core.Either
import arrow.core.getOrElse
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
                store.store.state.plugin
                    .map { plugin ->
                        it.getWikilinkResourcePath(
                            plugin.app.vault,
                            plugin.app.metadataCache
                        ).bind()
                    }
                    .getOrElse {
                        raise(MinionError.StateError("No plugin defined in State"))
                    }
            }.bind()
    }

    fun FlowContent.outputSubtasks(item: Item, store: MinionStore) {
        item.task.map { task ->
            task.subtasks.forEach { subtask ->
                if (!subtask.completed) {
                    div(classes = "mi-codeblock-task-subtask") {
                        checkBoxInput {
                            store.store.state.plugin.map { plugin ->
                                onClickFunction = {
                                    store.dispatch(
                                        (TaskThunks.completeSubtask(
                                            plugin.app,
                                            task,
                                            subtask
                                        ))
                                    )
                                }
                            }
                        }
                        span(classes = "mi-codeblock-task-subtask-content") {
                            outputStyledContent(subtask.content, store)
                        }
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
                store.store.state.plugin.map { plugin ->
                    onClickFunction = {
                        UpdateDataviewValue(
                            fileData,
                            config.groupByField,
                            fileData.dataview[DataviewField(config.groupByField)].toOption(),
                            store.store.state.dataviewValueCache[DataviewField(config.groupByField)]!!,
                            store,
                            plugin.app
                        ).open()
                    }
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
                store.store.state.plugin.map { plugin ->
                    onClickFunction = {
                        KanbanStatusSelectModal(store, task, plugin.app).open()
                    }
                }
            }
        }
    }
}}
