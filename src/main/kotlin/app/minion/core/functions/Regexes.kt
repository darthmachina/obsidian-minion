package app.minion.core.functions

import app.minion.core.model.RepeatSpan

val dataviewRegex = """^([\w ]*):: (.*)$""".toRegex(RegexOption.MULTILINE)
val inlineDataviewRegex = """\[(\w*):: ([\w! \-_:]*)\]""".toRegex()
val taskSyntaxRegex = "^(\\s)*- \\[[xX ]\\] ".toRegex()
val noteSyntaxRegex = "^(\\s)*- ".toRegex()
val allTagsRegex = """#([a-zA-Z][0-9a-zA-Z-_/]*)""".toRegex()
val spanRegex = RepeatSpan.getAllSpans().joinToString("|")
val repeatItemRegex = """($spanRegex)(!?)(: ([0-9]{1,2}))?""".toRegex()

val PARENT_TAG_REGEX = "#.*/".toRegex()
val WIKILINK = """\[\[([A-Za-z0-9 .'+_-]+)\]\]"""
val WIKILINK_REGEX = WIKILINK.toRegex()
val WIKILINK_EMBED_REGEX = ("!$WIKILINK").toRegex()
val BOLD_REGEX = """\*\*([A-Za-z0-9 {}<>"#=/_.-]+)\*\*""".toRegex()
val ITALIC_REGEX = """\*([A-Za-z0-9 {}<>"#=/_.-]+)\*""".toRegex()
val CODE_REGEX = """`([A-Za-z0-9 {}<>"#=/_.-]+)`""".toRegex()
