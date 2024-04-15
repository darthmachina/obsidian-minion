package app.minion.shell.view.codeblock

import app.minion.core.MinionError
import app.minion.core.functions.PARENT_TAG_REGEX
import app.minion.core.functions.StatisticsFunctions.Companion.calculateTotalCount
import app.minion.core.model.Content
import app.minion.core.store.MinionStore
import app.minion.shell.view.ViewFunctions.Companion.outputStyledContent
import app.minion.shell.view.ViewItems
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

    fun HTMLElement.outputItemStats(items: List<ViewItems>) {
        this.append.div(classes = "mi-codeblock-item-count") {
            +"Task Count: ${items.calculateTotalCount()}"
        }
    }

    fun FlowContent.outputGroupLabel(label: String, store: MinionStore) {
        h4(classes = "mi-codeblock-group-label") {
            if (label.contains(":")) {
                outputStyledContent(Content(label.split(":")[1]), store)
            } else {
                outputStyledContent(Content(label
                    .replace(PARENT_TAG_REGEX, "")
                    .replace("#", "")
                    .replaceFirstChar(Char::uppercaseChar)), store)
            }
        }
    }
}}
