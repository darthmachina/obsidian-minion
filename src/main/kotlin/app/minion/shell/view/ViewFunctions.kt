package app.minion.shell.view

import MetadataCache
import Vault
import app.minion.core.MinionError
import app.minion.core.functions.BOLD_REGEX
import app.minion.core.functions.CODE_REGEX
import app.minion.core.functions.DateTimeFunctions.Companion.asString
import app.minion.core.functions.DateTimeFunctions.Companion.isInPast
import app.minion.core.functions.ITALIC_REGEX
import app.minion.core.functions.ImageFunctions.Companion.getImageName
import app.minion.core.functions.WIKILINK_REGEX
import app.minion.core.model.Content
import app.minion.core.model.DateTime
import app.minion.core.model.Filename
import app.minion.core.model.Tag
import app.minion.core.model.Task
import app.minion.core.store.MinionStore
import app.minion.shell.functions.VaultFunctions.Companion.openSourceFile
import app.minion.shell.functions.VaultFunctions.Companion.sourceFileExists
import app.minion.shell.thunk.TaskThunks
import app.minion.shell.view.codeblock.CodeBlockConfig
import arrow.core.Either
import arrow.core.raise.either
import arrow.core.toOption
import kotlinx.html.FlowContent
import kotlinx.html.FlowOrPhrasingContent
import kotlinx.html.checkBoxInput
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.js.onClickFunction
import kotlinx.html.span
import kotlinx.html.style
import mu.KotlinLogging
import org.w3c.dom.HTMLElement

private val logger = KotlinLogging.logger("ViewFunctions")

interface ViewFunctions { companion object {
    fun HTMLElement.maybeOutputHeading(config: CodeBlockConfig) {
        if (config.heading.isNotEmpty()) {
            append.div(classes = "mi-codeblock-heading") {
                +config.heading
            }
        }
    }

    fun FlowOrPhrasingContent.outputStyledContent(content: Content, store: MinionStore) {
        content.tokenize()
            .forEach { token ->
                if (token.startsWith("!l")) {
                    if (sourceFileExists(Filename(token.drop(2)), store.store.state.plugin.app.metadataCache)) {
                        // Wikilink
                        span(classes = "mi-wikilink") {
                            +token.drop(2)
                            onClickFunction = {
                                openSourceFile(Filename(token.drop(2)), store.store.state.plugin.app)
                            }
                        }
                    } else {
                        span(classes = "mi-wikilink-none") { +token.drop(2) }
                    }
                } else if (token.startsWith("!b")) {
                    span(classes = "mi-bold") {
                        +token.drop(2)
                    }
                } else if (token.startsWith("!i")) {
                    span(classes = "mi-italic") {
                        +token.drop(2)
                    }
                } else if (token.startsWith("!c")) {
                    span(classes = "mi-inline-code") {
                        +token.drop(2)
                    }
                } else {
                    span { +token }
                }
            }
    }

    fun FlowContent.outputCheckbox(task: Task, store: MinionStore) {
        checkBoxInput {
            task.tags
                .intersect(store.store.state.settings.lifeAreas.keys.map { Tag(it) }.toSet())
                .let {
                    if (it.isNotEmpty()) {
                        logger.debug { "Applying style to checkbox: $it" }
                        attributes["style"] = "border: 1px solid ${store.store.state.settings.lifeAreas[it.first().v]!!}"
                    }
                }

            onClickFunction = {
                store.dispatch(TaskThunks.completeTask(store.store.state.plugin.app, task))
            }
        }
    }

    fun Content.tokenize() : List<String> {
        return v.parseMarkdownLinks()
            .flatMap { it.parseBold() }
            .flatMap { it.parseItalic() }
            .flatMap { it.parseCode() }
    }

    /**
     * Parses out wiki-style markdown links.
     *
     * @return a tokenized list with all links starting with !
     */
    fun String.parseMarkdownLinks() : List<String> {
        return this.replace(WIKILINK_REGEX, "|!l\$1|").split("|")
    }

    fun String.parseBold() : List<String> {
        return this.replace(BOLD_REGEX, "|!b\$1|").split("|")
    }

    fun String.parseItalic() : List<String> {
        return this.replace(ITALIC_REGEX, "|!i\$1|").split("|")
    }

    fun String.parseCode() : List<String> {
        return this.replace(CODE_REGEX, "|!c\$1|").split("|")
    }

    fun String.getWikilinkResourcePath(vault: Vault, metadataCache: MetadataCache)
    : Either<MinionError, String> = either {
        logger.debug { "getWikilinkResourcePath: ${this@getWikilinkResourcePath}" }
        this@getWikilinkResourcePath
            .getImageName()
            .map { it.getImageResourcePath(vault, metadataCache).bind() }
            .bind()
    }

    fun String.getImageResourcePath(vault: Vault, metadataCache: MetadataCache)
    : Either<MinionError, String> = either {
        logger.debug { "getImageResourcePath: ${this@getImageResourcePath}" }
        metadataCache
            .getFirstLinkpathDest(this@getImageResourcePath, "")
            .toOption()
            .map { vault.getResourcePath(it) }
            .toEither {
                MinionError.ImageNotFoundError("${this@getImageResourcePath} not found")
            }
            .bind()
    }

    fun FlowContent.outputDue(due: DateTime) {
        span(classes = "mi-codeblock-task-content-due") {
            if (due.isInPast()) {
                style = "color: crimson"
            }
            +"[${due.asString()}]"
        }
    }
}}
