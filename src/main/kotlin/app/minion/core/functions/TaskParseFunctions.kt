package app.minion.core.functions

import app.minion.core.MinionError
import app.minion.core.model.COMPLETED_ON_PROPERTY
import app.minion.core.model.Content
import app.minion.core.model.DataviewField
import app.minion.core.model.DataviewValue
import app.minion.core.model.Filename
import app.minion.core.model.ListItemFileInfo
import app.minion.core.model.Tag
import app.minion.core.model.Task
import app.minion.shell.functions.allTagsRegex
import app.minion.shell.functions.inlineDataviewRegex
import app.minion.shell.functions.noteSyntaxRegex
import app.minion.shell.functions.taskSyntaxRegex
import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.raise.either
import arrow.core.toOption
import kotlinx.datetime.Instant

interface TaskParseFunctions { companion object {
    fun String.toTask(source: Filename, line: Int, completed: Boolean) : Either<MinionError.TaskConversionError, Task> = either {
        val dataviewFields = inlineDataviewRegex.findAll(this@toTask)
            .associate { DataviewField(it.groupValues[1]) to DataviewValue(it.groupValues[2]) }
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

    fun findDataviewCompletedOn(dataview: Map<DataviewField, DataviewValue>) : Either<MinionError.TaskConversionError, Option<Instant>> = either {
        if (dataview.containsKey(COMPLETED_ON_PROPERTY)) {
            Instant.fromEpochMilliseconds(dataview[COMPLETED_ON_PROPERTY]!!.v.toLong()).toOption()
        } else {
            None
        }
    }
}}
