package app.minion.shell.view.codeblock

import app.minion.core.model.DataviewField
import app.minion.core.model.DataviewValue
import app.minion.core.model.File
import app.minion.core.model.FileData
import app.minion.core.model.Filename
import app.minion.core.store.MinionStore
import app.minion.shell.view.codeblock.components.CodeBlockCard.Companion.outputCard
import app.minion.shell.view.codeblock.components.CodeBlockCard.Companion.outputPageCard
import arrow.core.None
import kotlinx.html.dom.append
import kotlinx.html.js.div
import kotlinx.html.span
import mu.KotlinLogging
import org.w3c.dom.HTMLElement

private val logger = KotlinLogging.logger("CodeBlockCardTestView")

interface CodeBlockCardTestView { companion object {
    fun HTMLElement.addCardTestView(config: CodeBlockConfig, store: MinionStore) {
        logger.debug { "addCardTestView()" }
        classList.add("mi-codeblock")
        addCards(config, store)
    }

    fun HTMLElement.addCards(config: CodeBlockConfig, store: MinionStore) {
        val fileData1 = FileData(
            name = Filename("Test 1"),
            path = File("Test1.md"),
            dataview = mapOf(DataviewField(FIELD_IMAGE) to DataviewValue("![[dune2021.jpg]]"))
        )
        val fileData2 = FileData(
            name = Filename("Test 2 with a really long card title that should wrap in the display"),
            path = File("Test2.md")
        )
        this.append.div(classes = "mi-codeblock-page-gallery") {
            outputPageCard(fileData1, config, store)
            outputPageCard(fileData2, config, store)
        }
    }
}}
