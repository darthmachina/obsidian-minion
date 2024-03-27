package app.minion.shell.view.codeblock

import app.minion.core.MinionError
import app.minion.core.functions.PARENT_TAG_REGEX
import app.minion.core.functions.StatisticsFunctions.Companion.calculateTotalCount
import app.minion.core.model.Task
import kotlinx.dom.clear
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.h4
import org.w3c.dom.HTMLElement

interface CodeBlockFunctions { companion object {
    fun HTMLElement.showError(error: MinionError) {
        this.clear()
        this.append.div { +error.message }
    }

    fun HTMLElement.outputHeading(heading: String) {
        this.append.div(classes = "mi-codeblock-heading") {
            +heading
        }
    }

    fun HTMLElement.outputTaskStats(tasks: Map<String, List<Task>>) {
        this.append.div(classes = "mi-codeblock-item-count") {
            +"Task Count: ${tasks.calculateTotalCount()}"
        }
    }

    fun FlowContent.outputGroupLabel(label: String) {
        h4(classes = "mi-codeblock-group-label") {
            +label
                .replace(PARENT_TAG_REGEX, "")
                .replaceFirstChar(Char::uppercaseChar)
        }
    }
}}
