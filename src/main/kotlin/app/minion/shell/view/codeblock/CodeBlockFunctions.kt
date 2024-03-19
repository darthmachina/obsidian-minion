package app.minion.shell.view.codeblock

import app.minion.core.MinionError
import kotlinx.dom.clear
import kotlinx.html.div
import kotlinx.html.dom.append
import org.w3c.dom.HTMLElement

interface CodeBlockFunctions { companion object {
    fun HTMLElement.showError(error: MinionError) {
        this.clear()
        this.append.div { +error.message }
    }
}}