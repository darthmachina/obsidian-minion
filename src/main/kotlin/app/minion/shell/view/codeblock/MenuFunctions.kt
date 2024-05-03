package app.minion.shell.view.codeblock

import app.minion.core.functions.SectionFunctions.Companion.filterByProject
import app.minion.core.model.todoist.TodoistTask
import app.minion.core.store.MinionStore
import app.minion.shell.view.ICON_GROUP
import app.minion.shell.view.modal.ChangeSectionModal
import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.js.onClickFunction
import kotlinx.html.title
import kotlinx.html.unsafe

interface MenuFunctions { companion object {
    fun FlowContent.createChangeSectionMenuItem(task: TodoistTask, config: CodeBlockConfig, store: MinionStore)
    : FlowContent.() -> Unit {
        return {
            a {
                title = "Change section"
                unsafe { +ICON_GROUP }
                onClickFunction = {
                    ChangeSectionModal(
                        task,
                        store.getState().sections.filterByProject(task.project),
                        store,
                        store.store.state.plugin.app
                    ).open()
                }
            }
        }
    }
}}