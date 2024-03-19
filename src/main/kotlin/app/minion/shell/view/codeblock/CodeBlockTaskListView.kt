package app.minion.shell.view.codeblock

import app.minion.core.MinionError
import app.minion.core.functions.DateTimeFunctions.Companion.asString
import app.minion.core.functions.DateTimeFunctions.Companion.isInPast
import app.minion.core.functions.TaskStatisticsFunctions.Companion.completedSubtaskPercent
import app.minion.core.functions.TaskTagFunctions.Companion.collectTags
import app.minion.core.model.DateTime
import app.minion.core.model.ListItemFileInfo
import app.minion.core.model.Tag
import app.minion.core.model.Task
import app.minion.core.store.MinionStore
import app.minion.shell.functions.VaultFunctions.Companion.openSourceFile
import app.minion.shell.thunk.TaskThunks
import app.minion.shell.view.ViewFunctions.Companion.outputStyledContent
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.outputHeading
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.outputTaskStats
import app.minion.shell.view.codeblock.CodeBlockTaskFunctions.Companion.applyCodeBlockConfig
import app.minion.shell.view.codeblock.CodeBlockTaskFunctions.Companion.maybeAddProperties
import app.minion.shell.view.codeblock.CodeBlockTaskFunctions.Companion.removeConfigTags
import app.minion.shell.view.iconKanban
import app.minion.shell.view.iconMenu
import app.minion.shell.view.iconRepeat
import app.minion.shell.view.modal.KanbanStatusSelectModal
import arrow.core.Option
import io.kvision.state.sub
import kotlinx.dom.clear
import kotlinx.html.FlowContent
import kotlinx.html.a
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

        val updatedConfig = config.maybeAddProperties()
        store
            .sub { it.tasks.applyCodeBlockConfig(updatedConfig) }
            .subscribe { tasks ->
                logger.debug { "Task list updated, running updateTasks(): $tasks" }
                tasks
                    .map {
                        updateTasks(it, this, store, updatedConfig)
                    }
            }

    }

    fun showError(error: MinionError, element: HTMLElement) {
        element.clear()
        element.append.div { +error.message }
    }

    fun updateTasks(tasks: Map<String, List<Task>>, element: HTMLElement, store: MinionStore, config: CodeBlockConfig) {
        element.clear()
        if (config.heading.isNotEmpty()) {
            element.outputHeading(config.heading)
        }

        if (tasks.isNotEmpty()) {
            tasks.forEach { task ->
                element.append.div(classes = "mi-codeblock-task") {
                    outputTask(task.value, store, config)
                }
            }
        }
        element.outputTaskStats(tasks)
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
            if (config.properties.contains(PROPERTY_DUE)) {
                task.dueDate.map { due ->
                    outputDue(due)
                }
            }
            span {
                outputStyledContent(task.content, store)
            }
            if (config.properties.contains(PROPERTY_TAGS)) {
                with(task.collectTags()) {
                    if (isNotEmpty()) {
                        outputTags(this, config)
                    }
                }
            }
            task.repeatInfo.map {
                span(classes = "mi-icon") {
                    title = it.asString()
                    unsafe { +iconRepeat }
                }
            }
            if (config.properties.contains(PROPERTY_SOURCE)) {
                outputSource(task.fileInfo, store)
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
            div(classes = "mi-codeblock-menu-container") {
                span(classes = "mi-icon mi-button") {
                    unsafe { +iconMenu }
                }
                div(classes = "mi-codeblock-menu") {
                    a {
                        title = "Change kanban status"
                        unsafe { +iconKanban }
                        onClickFunction = {
                            KanbanStatusSelectModal(store, task, store.store.state.plugin.app).open()
                        }
                    }
                }
            }
        }
    }

    fun FlowContent.outputDue(due: DateTime) {
        span(classes = "mi-codeblock-task-content-due") {
            if (due.isInPast()) {
                style = "color: crimson"
            }
            +"[${due.asString()}]"
        }
    }

    fun FlowContent.outputTags(tags: Set<Tag>, config: CodeBlockConfig) {
        span(classes = "mi-codeblock-task-content-tags") {
            tags
                .removeConfigTags(config)
                .forEach { tag ->
                    span(classes = "mi-codeblock-task-content-tags-tag") { +"#${tag.v}" }
                }
        }
    }

    fun FlowContent.outputSource(fileInfo: ListItemFileInfo, store: MinionStore) {
        span(classes = "mi-codeblock-task-source") {
            span { +"(" }
            span(classes = "mi-codeblock-source-link") {
                +fileInfo.file.v
                onClickFunction = {
                    openSourceFile(fileInfo.file, store.store.state.plugin.app)
                }
            }
            span { +")" }
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
}}
