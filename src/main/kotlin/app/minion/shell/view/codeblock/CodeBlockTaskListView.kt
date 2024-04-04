package app.minion.shell.view.codeblock

import app.minion.core.MinionError
import app.minion.core.functions.DateTimeFunctions.Companion.asString
import app.minion.core.functions.DateTimeFunctions.Companion.isInPast
import app.minion.core.functions.TaskStatisticsFunctions.Companion.completedSubtaskPercent
import app.minion.core.functions.TaskTagFunctions.Companion.collectTags
import app.minion.core.model.DateTime
import app.minion.core.model.Filename
import app.minion.core.model.ListItemFileInfo
import app.minion.core.model.Tag
import app.minion.core.model.Task
import app.minion.core.store.MinionStore
import app.minion.shell.functions.VaultFunctions.Companion.openSourceFile
import app.minion.shell.thunk.TaskThunks
import app.minion.shell.view.ViewFunctions.Companion.outputCheckbox
import app.minion.shell.view.ViewFunctions.Companion.outputDue
import app.minion.shell.view.ViewFunctions.Companion.outputSourceLink
import app.minion.shell.view.ViewFunctions.Companion.outputStyledContent
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.outputGroupLabel
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.outputHeading
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.outputTaskStats
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.showError
import app.minion.shell.view.codeblock.CodeBlockTaskFunctions.Companion.applyCodeBlockConfig
import app.minion.shell.view.codeblock.CodeBlockTaskFunctions.Companion.maybeAddProperties
import app.minion.shell.view.codeblock.CodeBlockTaskFunctions.Companion.removeConfigTags
import app.minion.shell.view.iconImportant
import app.minion.shell.view.iconKanban
import app.minion.shell.view.iconMenu
import app.minion.shell.view.iconRepeat
import app.minion.shell.view.iconUrgent
import app.minion.shell.view.modal.KanbanStatusSelectModal
import arrow.core.Option
import arrow.core.toOption
import io.kvision.state.sub
import kotlinx.dom.clear
import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.checkBoxInput
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.h3
import kotlinx.html.h4
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
                error.map { this.showError(it) }
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

    fun updateTasks(tasks: Map<String, List<Task>>, element: HTMLElement, store: MinionStore, config: CodeBlockConfig) {
        element.clear()
        if (config.heading.isNotEmpty()) {
            element.outputHeading(config.heading)
        }

        if (tasks.isNotEmpty()) {
            element.append.div {
                if (config.groupByOrder.isEmpty()) {
                    tasks.forEach { entry ->
                        outputGroupDiv(entry.key, entry.value, config, store)
                    }
                } else {
                    config.groupByOrder.forEach { group ->
                        logger.debug { "Outputting group $group" }
                        if (group.contains(":")) {
                            group.split(":")
                                .let {
                                    outputGroupWithLabel(it[0], it[1], tasks, config, store)
                                }
                        } else {
                            outputGroupWithLabel(group, group, tasks, config, store)
                        }
                    }
                    // Output any remaining entries
                    tasks
                        .filter { entry ->
                            !config.groupByOrder.any { group ->
                                group.startsWith(entry.key)
                            }
                        }
                        .forEach { entry ->
                            outputGroupDiv(entry.key, entry.value, config, store)
                        }
                }
            }
        }
        element.outputTaskStats(tasks)
    }

    fun FlowContent.outputGroupWithLabel(group: String, label: String, tasks: Map<String, List<Task>>, config: CodeBlockConfig, store: MinionStore) {
        logger.debug { "outputGroupWithLabel: $group, $label" }
        tasks[group]
            .toOption().toEither {
                MinionError.GroupByNotFoundError("$group not found in results")
            }
            .map { outputGroupDiv(label, it, config, store) }
            .mapLeft { logger.warn { "$it" } }
    }

    fun FlowContent.outputGroupDiv(label: String, tasks: List<Task>, config: CodeBlockConfig, store: MinionStore) {
        if (label == GROUP_BY_SINGLE) {
            outputTaskList(tasks, config, store)
        } else {
            div {
                outputGroupLabel(label)
                outputTaskList(tasks, config, store)
            }
        }
    }

    fun FlowContent.outputTaskList(tasks: List<Task>, config: CodeBlockConfig, store: MinionStore) {
        div {
            tasks.forEach { task ->
                div(classes = "mi-codeblock-task") {
                    outputTask(task, store, config)
                }
            }
        }
    }

    fun FlowContent.outputTask(task: Task, store: MinionStore, config: CodeBlockConfig) {
        outputCheckbox(task, store)
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
            if (task.important && config.properties.contains(PROPERTY_EISENHOWER)) {
                span(classes = "mi-icon") {
                    title = "Important"
                    unsafe { +iconImportant }
                }
            }
            if (task.urgent && config.properties.contains(PROPERTY_EISENHOWER)) {
                span(classes = "mi-icon") {
                    title = "Urgent"
                    unsafe { +iconUrgent }
                }
            }
            task.repeatInfo.map {
                span(classes = "mi-icon") {
                    title = it.asString()
                    unsafe { +iconRepeat }
                }
            }
            if (config.properties.contains(PROPERTY_SOURCE)) {
                outputSource(task.fileInfo.file, store)
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

    fun FlowContent.outputTags(tags: Set<Tag>, config: CodeBlockConfig) {
        span(classes = "mi-codeblock-task-content-tags") {
            tags
                .removeConfigTags(config)
                .forEach { tag ->
                    span(classes = "mi-codeblock-task-content-tags-tag") { +"#${tag.v}" }
                }
        }
    }

    fun FlowContent.outputSource(fileInfo: Filename, store: MinionStore) {
        span(classes = "mi-codeblock-task-source") {
            span { +"(" }
            outputSourceLink(fileInfo, store)
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
