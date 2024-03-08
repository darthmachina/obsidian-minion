package app.minion.shell.view

import app.minion.core.model.Content
import app.minion.core.model.Filename
import app.minion.core.store.MinionStore
import app.minion.shell.functions.BOLD_REGEX
import app.minion.shell.functions.CODE_REGEX
import app.minion.shell.functions.ITALIC_REGEX
import app.minion.shell.functions.VaultFunctions.Companion.openSourceFile
import app.minion.shell.functions.VaultFunctions.Companion.sourceFileExists
import app.minion.shell.functions.WIKILINK_REGEX
import kotlinx.html.FlowOrPhrasingContent
import kotlinx.html.js.onClickFunction
import kotlinx.html.span

interface ViewFunctions { companion object {
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
}}
