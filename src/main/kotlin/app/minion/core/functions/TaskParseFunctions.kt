package app.minion.core.functions

import app.minion.core.MinionError
import app.minion.core.model.*
import app.minion.shell.functions.allTagsRegex
import app.minion.shell.functions.inlineDataviewRegex
import app.minion.shell.functions.noteSyntaxRegex
import app.minion.shell.functions.taskSyntaxRegex
import arrow.core.*
import arrow.core.raise.either
import kotlinx.datetime.Instant

interface TaskParseFunctions { companion object {
    fun String.toTask(source: Filename, line: Int, completed: Boolean) : Either<MinionError, Task> = either {
        val dataviewFields = inlineDataviewRegex.findAll(this@toTask)
            .associate { DataviewField(it.groupValues[1]) to DataviewValue(it.groupValues[2]) }
        val dueDate = findDataviewDueDate(dataviewFields).bind()
        val completedOn = findDataviewCompletedOn(dataviewFields).bind()

        val tags = allTagsRegex
            .findAll(this@toTask)
            .map {
                Tag(it.groupValues[1])
            }
            .toSet()

        Task(
            Content(this@toTask.extractTaskDescription()),
            ListItemFileInfo(source, line, this@toTask),
            tags = tags,
            dueDate = dueDate,
            completedOn = completedOn
        )
    }

    fun String.extractTaskDescription() : String {
        return this
            .replace(allTagsRegex, "")
            .replace(taskSyntaxRegex, "")
            .replace(inlineDataviewRegex, "")
            .replace(noteSyntaxRegex, "")
            .trim()
    }

    fun findDataviewDueDate(dataview: Map<DataviewField, DataviewValue>) : Either<MinionError, Option<DateTime>> = either {
        if (dataview.containsKey(DUE_ON_PROPERTY)) {
            DateTimeFunctions.parseDateTime(dataview[DUE_ON_PROPERTY]!!.v)
                .map { it.toOption() }
                .bind()
        } else {
            None
        }
    }

    fun findDataviewCompletedOn(dataview: Map<DataviewField, DataviewValue>) : Either<MinionError, Option<Instant>> = either {
        if (dataview.containsKey(COMPLETED_ON_PROPERTY)) {
            Instant.fromEpochMilliseconds(dataview[COMPLETED_ON_PROPERTY]!!.v.toLong()).toOption()
        } else {
            None
        }
    }
}}
