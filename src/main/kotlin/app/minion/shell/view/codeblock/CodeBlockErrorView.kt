package app.minion.shell.view.codeblock

import app.minion.core.MinionError
import kotlinx.dom.clear
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.js.div
import org.w3c.dom.HTMLElement

class CodeBlockErrorView { companion object {
    fun HTMLElement.addErrorView(error: MinionError) {
        classList.add("mi-codeblock")
        clear()
        append.div {
            div {
                +error.message
            }
            error.throwable.map {
                div {
                    +(it.message ?: "")
                }
            }
        }
    }
}}