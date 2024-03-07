package app.minion.core.functions

import app.minion.core.model.DateTime
import app.minion.core.model.RepeatInfo
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

        markdownElements.add(if (completedOn.isSome()) "- [x]" else "- [ ] ")
        markdownElements.add(content.v)
        if (tags.isNotEmpty()) {
            markdownElements.add(tags.joinToString(" ") { tag -> "#${tag.v}"})
        }
        dueDate.map { markdownElements.add("${it.toMarkdown("due")} ") }
        hideUntil.map { markdownElements.add("${it.toMarkdown("hide")} ") }
        repeatInfo.map { markdownElements.add("${it.toMarkdown()} ") }
        completedOn.map { markdownElements.add("${it.asMarkdown()} ") }

        return markdownElements.joinToString(" ")
    }

    fun DateTime.toMarkdown(label: String) : String {
        return "[$label:: ${this.date.year}-${this.date.monthNumber}-${this.date.dayOfMonth}${this.time.toMarkdown()}]"
    }

    fun Option<LocalTime>.toMarkdown() : String {
        return this
            .map {
                " ${it.hour}:${it.minute}"
            }
            .getOrElse { "" }
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