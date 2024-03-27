package app.minion.core.functions

import app.minion.core.MinionError
import app.minion.core.model.*
import arrow.core.*
import arrow.core.raise.either
import kotlinx.datetime.Instant
import mu.KotlinLogging

private val logger = KotlinLogging.logger("TaskParseFunctions")

interface TaskParseFunctions { companion object {
    fun String.toTask(source: Filename, path: File, line: Int, completed: Boolean) : Either<MinionError, Task> = either {
        logger.debug { "toTask()" }
        val dataviewFields = inlineDataviewRegex.findAll(this@toTask)
            .associate { DataviewField(it.groupValues[1]) to DataviewValue(it.groupValues[2]) }
        logger.debug { "- dataviewFields: $dataviewFields" }
        val dueDate = findDataviewDueDate(dataviewFields).bind()
        val repeatInfo = findDataviewRepeatInfo(dataviewFields).bind()
        val completedOn = findDataviewCompletedOn(dataviewFields).bind()

        val tags = allTagsRegex
            .findAll(this@toTask)
            .map {
                Tag(it.groupValues[1].trim())
            }
            .toSet()

        Task(
            Content(this@toTask.extractTaskDescription()),
            ListItemFileInfo(source, path, line, this@toTask),
            tags = tags,
            dueDate = dueDate,
            repeatInfo = repeatInfo,
            completedOn = completedOn,
            completed = completed
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

    fun String.extractTagSet() : Set<Tag> {
        return allTagsRegex
            .findAll(this)
            .map { Tag(it.groupValues[1]) }
            .toSet()
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

    fun findDataviewRepeatInfo(dataview: Map<DataviewField, DataviewValue>) : Either<MinionError, Option<RepeatInfo>> = either {
        if (dataview.containsKey(REPEAT_PROPERTY)) {
            runCatching {
                repeatItemRegex.find(dataview[REPEAT_PROPERTY]!!.v)
                    .toOption()
                    .map { it.groupValues }
                    .map { repeatMatches ->
                        RepeatInfo(
                            repeatMatches[4].toInt(),
                            RepeatSpan.findForSpan(repeatMatches[1]),
                            repeatMatches[2] == "!"
                        ).toOption()
                    }
                    .getOrElse { None }
            }.getOrElse {
                raise(MinionError.RepeatInfoParseError(
                    "Cannot parse repeating information: ${dataview[REPEAT_PROPERTY]}",
                    it.toOption()
                ))
            }
        } else {
            None
        }
    }

    fun findDataviewCompletedOn(dataview: Map<DataviewField, DataviewValue>) : Either<MinionError, Option<Instant>> = either {
        runCatching {
            if (dataview.containsKey(COMPLETED_ON_PROPERTY)) {
                Instant.fromEpochMilliseconds(dataview[COMPLETED_ON_PROPERTY]!!.v.toLong()).toOption()
            } else {
                None
            }
        }.getOrElse {
            logger.warn { "Error parsing Instant, returning no Date" }
            None
        }
    }
}}
