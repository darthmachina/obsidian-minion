package app.minion.shell.view.codeblock

import app.minion.core.model.Filename
import app.minion.core.model.Tag
import app.minion.core.store.MinionStore
import app.minion.shell.thunk.TaskThunks
import app.minion.shell.view.ICON_DATE
import app.minion.shell.view.ViewFunctions.Companion.outputCheckbox
import app.minion.shell.view.ViewFunctions.Companion.outputDue
import app.minion.shell.view.ViewFunctions.Companion.outputSourceLink
import app.minion.shell.view.ViewFunctions.Companion.outputStyledContent
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.outputGroupLabel
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.outputHeading
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.outputItemStats
import app.minion.shell.view.codeblock.CodeBlockFunctions.Companion.showError
import app.minion.shell.view.codeblock.CodeBlockTaskFunctions.Companion.applyCodeBlockConfig
import app.minion.shell.view.codeblock.CodeBlockTaskFunctions.Companion.maybeAddProperties
import app.minion.shell.view.codeblock.CodeBlockTaskFunctions.Companion.removeConfigTags
import app.minion.shell.view.ICON_IMPORTANT
import app.minion.shell.view.ICON_KANBAN
import app.minion.shell.view.ICON_MENU
import app.minion.shell.view.ICON_REPEAT
import app.minion.shell.view.ICON_URGENT
import app.minion.shell.view.Item
import app.minion.shell.view.PropertyType
import app.minion.shell.view.ViewFunctions.Companion.outputIcon
import app.minion.shell.view.ViewItems
import app.minion.shell.view.ViewModelFunctions.Companion.getPropertyValue
import app.minion.shell.view.modal.ChangeTaskDateModal
import app.minion.shell.view.modal.KanbanStatusSelectModal
import arrow.core.getOrElse
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

    fun updateTasks(viewItems: List<ViewItems>, element: HTMLElement, store: MinionStore, config: CodeBlockConfig) {
        element.clear()
        if (config.heading.isNotEmpty()) {
            element.outputHeading(config.heading)
        }

        if (viewItems.isNotEmpty()) {
            element.append.div {
                viewItems.forEach { viewItem ->
                    outputGroupDiv(viewItem.group, viewItem.items, config, store)
                }
            }
        }
        element.outputItemStats(viewItems)
    }

    fun FlowContent.outputGroupDiv(label: String, items: List<Item>, config: CodeBlockConfig, store: MinionStore) {
        if (label == GROUP_BY_SINGLE) {
            outputItems(items, config, store)
        } else {
            div {
                outputGroupLabel(label, store)
                outputItems(items, config, store)
            }
        }
    }

    fun FlowContent.outputItems(items: List<Item>, config: CodeBlockConfig, store: MinionStore) {
        div {
            items.forEach { task ->
                div(classes = "mi-codeblock-task") {
                    outputItem(task, store, config)
                }
            }
        }
    }

    fun FlowContent.outputItem(item: Item, store: MinionStore, config: CodeBlockConfig) {
        outputCheckbox(item, store)
        outputContent(item, store, config)
        outputSubtasks(item, store)
        outputNotes(item, store)
        hr {}
    }

    fun FlowContent.outputContent(item: Item, store: MinionStore, config: CodeBlockConfig) {
        span(classes = "mi-codeblock-task-content") {
            item.getPropertyValue(PropertyType.DUE).map { due ->
                outputDue(
                    due,
                    item
                        .getPropertyValue(PropertyType.DUE_IN_PAST)
                        .map { it == "true" }
                        .getOrElse { false }
                )
            }

            span { +" " }
            span {
                outputStyledContent(item.content, store)
            }

            span { +" " }
            item.getPropertyValue(PropertyType.TAGS).map { tags ->
                if (tags.isNotEmpty()) {
                    tags
                        .split(" ")
                        .map { Tag(it) }
                        .toSet()
                        .let {
                            outputTags(it, config)
                        }
                }
            }

            span { +" " }
            item.getPropertyValue(PropertyType.EISENHOWER).map {
                if (it.contains("i")) {
                    outputIcon(ICON_IMPORTANT, "Important")
                }
                if (it.contains("u")) {
                    outputIcon(ICON_URGENT, "Urgent")
                }
            }
            item.getPropertyValue(PropertyType.REPEAT_INFO).map {
                span(classes = "mi-icon") {
                    title = it
                    unsafe { +ICON_REPEAT }
                }
            }
            item.getPropertyValue(PropertyType.SOURCE).map {
                outputSource(it, store)
            }
            item.getPropertyValue(PropertyType.COMPLETED_SUBTASK_PERCENT).map {
                span(classes = "mi-codeblock-task-subtask-percentage") {
                    if (it == "100") {
                        style = "color: green"
                    }
                    +"$it%"
                }
            }
            item.task.onSome { task ->
                div(classes = "mi-codeblock-menu-container") {
                    span(classes = "mi-icon mi-button") {
                        unsafe { +ICON_MENU }
                    }
                    div(classes = "mi-codeblock-menu") {
                        a {
                            title = "Change kanban status"
                            unsafe { +ICON_KANBAN }
                            onClickFunction = {
                                KanbanStatusSelectModal(store, task, store.store.state.plugin.app).open()
                            }
                        }
                        a {
                            title = "Change date"
                            unsafe { +ICON_DATE }
                            onClickFunction = {
                                ChangeTaskDateModal(task, store, store.store.state.plugin.app).open()
                            }
                        }
                    }
                }
            }.onNone {
                logger.error { "No task set for item ${item.content.v}" }
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

    fun FlowContent.outputSource(source: String, store: MinionStore) {
        span(classes = "mi-codeblock-task-source") {
            span { +"(" }
            outputSourceLink(Filename(source), store)
            span { +")" }
        }
    }

    fun FlowContent.outputSubtasks(item: Item, store: MinionStore) {
        item.task.map { task ->
            task.subtasks.forEach { subtask ->
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

    fun FlowContent.outputNotes(item: Item, store: MinionStore) {
        item.task.map {task ->
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
    }
}}
