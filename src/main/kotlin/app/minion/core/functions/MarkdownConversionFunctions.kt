package app.minion.core.functions

import app.minion.core.functions.DateTimeFunctions.Companion.asString
import app.minion.core.functions.TaskTagFunctions.Companion.asString
import app.minion.core.model.DateTime
import app.minion.core.model.RepeatInfo
import app.minion.core.model.TagType
import app.minion.core.model.Task
import arrow.core.Option
import arrow.core.getOrElse
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import mu.KotlinLogging

private val logger = KotlinLogging.logger("MarkdownConversionFunctions")

interface MarkdownConversionFunctions { companion object {
    fun Task.toMarkdown() : String {
        val markdownElements = mutableListOf<String>()

        markdownElements.add(if (completedOn.isSome()) "- [x]" else "- [ ]")
        markdownElements.add(content.v)
        if (tags.isNotEmpty()) {
            markdownElements.add(tags.filter { it.type == TagType.TASK }.toSet().asString())
        }
        dueDate.map { markdownElements.add("${it.toMarkdown("due")} ") }
        hideUntil.map { markdownElements.add("${it.toMarkdown("hide")} ") }
        repeatInfo.map { markdownElements.add("${it.toMarkdown()} ") }
        completedOn.map { markdownElements.add("${it.asMarkdown()} ") }

        return markdownElements.joinToString(" ").trim()
    }

    /**
     * Converts a Pair that might contain a repeated Task into a one or two line Markdown string.
     */
    fun Pair<Task, Option<Task>>.toMarkdown() : String {
        logger.info { "Pair<Task, Option<Task>>.toMarkdown()" }
        return this.second
            .map { repeatedTask ->
                "${this.first.toMarkdown()}\n${repeatedTask.toMarkdown()}"
            }
            .getOrElse { this.first.toMarkdown() }
    }

    fun DateTime.toMarkdown(label: String) : String {
        return "[$label:: ${this.asString()}]"
    }

    fun RepeatInfo.toMarkdown() : String {
        return "[repeat:: ${this.span.span}${if (this.afterComplete) "!" else ""}: ${this.value}]"
    }

    fun Instant.asMarkdown() : String {
        return "[c:: ${this.toEpochMilliseconds()}]"
    }

    fun Task.completeAsMarkdown() : String {
        logger.info { "completeAsMarkdown()" }
        return this.fileInfo.original.replace("- [ ]", "- [x]")
    }
}}
