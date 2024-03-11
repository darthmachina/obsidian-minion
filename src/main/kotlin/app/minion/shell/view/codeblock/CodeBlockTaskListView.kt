package app.minion.shell.view.codeblock

import app.minion.core.MinionError
import app.minion.core.functions.DateTimeFunctions.Companion.asString
import app.minion.core.functions.DateTimeFunctions.Companion.isInPast
import app.minion.core.functions.TaskStatisticsFunctions.Companion.completedSubtaskPercent
import app.minion.core.functions.TaskTagFunctions.Companion.collectTags
import app.minion.core.model.Task
import app.minion.core.store.MinionStore
import app.minion.shell.functions.VaultFunctions.Companion.openSourceFile
import app.minion.shell.thunk.TaskThunks
import app.minion.shell.view.ViewFunctions.Companion.outputStyledContent
import app.minion.shell.view.codeblock.CodeBlockTaskFunctions.Companion.applyDue
import app.minion.shell.view.codeblock.CodeBlockTaskFunctions.Companion.applyExcludeTags
import app.minion.shell.view.codeblock.CodeBlockTaskFunctions.Companion.applyIncludeTags
import app.minion.shell.view.codeblock.CodeBlockTaskFunctions.Companion.removeConfigTags
import app.minion.shell.view.iconHash
import app.minion.shell.view.iconRepeat
import app.minion.shell.view.modal.KanbanStatusSelectModal
import io.kvision.state.sub
import kotlinx.dom.clear
import kotlinx.html.FlowContent
import kotlinx.html.checkBoxInput
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.hr
import kotlinx.html.js.onClickFunction
import kotlinx.html.li
import kotlinx.html.span
import kotlinx.html.style
import kotlinx.html.title
import kotlinx.html.ul
import kotlinx.html.unsafe
import mu.KotlinLogging
import org.w3c.dom.HTMLElement

private val logger = KotlinLogging.logger("CodeBlockTaskListView")

interface CodeBlockTaskListView { companion object {
    fun HTMLElement.addTaskListView(config: CodeBlockConfig, store: MinionStore) {
        classList.add("mi-codeblock")
        store
            .sub { it.error }
            .subscribe { error ->
                error.map { showError(it, this) }
            }
        store
            .sub { it.tasks.applyCodeBlockConfig(config) }
            .subscribe { tasks ->
                logger.debug { "Task list updated, running updateTasks(): $tasks" }
                updateTasks(tasks, this, store, config)
            }

    }

    fun showError(error: MinionError, element: HTMLElement) {
        element.clear()
        element.append.div { +error.message }
    }

    fun updateTasks(tasks: List<Task>, element: HTMLElement, store: MinionStore, config: CodeBlockConfig) {
        element.clear()
        if (config.heading.isNotEmpty()) {
            element.append.div(classes = "mi-codeblock-heading") {
                +config.heading
            }
        }

        tasks.forEach { task ->
            element.append.div(classes = "mi-codeblock-task") {
                outputTask(task, store, config)
            }
        }
        element.outputStats(tasks)
    }

    fun FlowContent.outputTask(task: Task, store: MinionStore, config: CodeBlockConfig) {
        checkBoxInput {
            onClickFunction = {
                store.dispatch(TaskThunks.completeTask(store.store.state.plugin.app, task))
            }
        }
        outputContent(task, store, config)
        outputSubtasks(task, store)
        outputNotes(task, store)
        hr {}
    }

    fun FlowContent.outputContent(task: Task, store: MinionStore, config: CodeBlockConfig) {
        span(classes = "mi-codeblock-task-content") {
            task.dueDate.map { due ->
                span(classes = "mi-codeblock-task-content-due") {
                    if (due.isInPast()) {
                        style = "color: crimson"
                    }
                    +"[${due.asString()}]"
                }
            }
            span {
                outputStyledContent(task.content, store)
            }
            with(task.collectTags()) {
                if(isNotEmpty()) {
                    span(classes = "mi-codeblock-task-content-tags") {
                        this@with
                            .removeConfigTags(config)
                            .forEach { tag ->
                                span(classes = "mi-codeblock-task-content-tags-tag") { +"#${tag.v}" }
                            }
                    }
                }
            }
            task.repeatInfo.map {
                span(classes = "mi-icon") {
                    title = it.asString()
                    unsafe { +iconRepeat }
                }
            }
            span(classes = "mi-codeblock-task-source") {
                span { +"(" }
                span(classes = "mi-codeblock-source-link") {
                    +task.fileInfo.file.v
                    onClickFunction = {
                        openSourceFile(task.fileInfo.file, store.store.state.plugin.app)
                    }
                }
                span { +")" }
            }
            task.completedSubtaskPercent()
                .map { percent ->
                    span(classes = "mi-codeblock-task-subtask-percentage") {
                        if (percent == 100) {
                            style = "color: green"
                        }
                        +"$percent%"
                    }
                }
            span(classes = "mi-icon mi-button") {
                title = "Change Kanban status"
                unsafe { +iconHash }
                onClickFunction = {
                    KanbanStatusSelectModal(store, task, store.store.state.plugin.app).open()
                }
            }
        }
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

    fun FlowContent.outputNotes(task: Task, store: MinionStore) {
        if (task.notes.isNotEmpty()) {
            ul(classes = "mi-codeblock-task-notes") {
                task.notes.forEach { note ->
                    li(classes = "mi-codeblock-task-notes-note") {
                        outputStyledContent(note.content, store)
                    }
                }
            }
        }
    }

    fun HTMLElement.outputStats(tasks: List<Task>) {
        this.append.div(classes = "mi-codeblock-item-count") {
            +"Task Count: ${tasks.size}"
        }
    }

    fun List<Task>.applyCodeBlockConfig(config: CodeBlockConfig) : List<Task> {
        return this
            .applyDue(config)
            .applyIncludeTags(config)
            .applyExcludeTags(config)
    }
}}
