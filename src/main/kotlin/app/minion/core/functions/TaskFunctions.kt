package app.minion.core.functions

import app.minion.core.MinionError
import app.minion.core.functions.MarkdownConversionFunctions.Companion.completeAsMarkdown
import app.minion.core.functions.RepeatingTaskFunctions.Companion.maybeRepeat
import app.minion.core.model.DataviewField
import app.minion.core.model.DataviewValue
import app.minion.core.model.MinionSettings
import app.minion.core.model.PageTaskFieldType
import app.minion.core.model.Tag
import app.minion.core.model.TagType
import app.minion.core.model.Task
import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.raise.either
import arrow.core.toOption
import kotlinx.datetime.Clock
import mu.KotlinLogging

private val logger = KotlinLogging.logger("TaskFunctions")

interface TaskFunctions { companion object {
    fun Task.complete(): Either<MinionError, Pair<Task, Option<Task>>> = either {
        logger.debug { "Task.complete()" }

        Pair(
            this@complete.copy(
                completedOn = Clock.System.now().toOption(),
                completed = true,
                repeatInfo = None
            ),
            this@complete.maybeRepeat()
        )
    }

    fun Task.completeSubtask(subtask: Task): Either<MinionError, Task> = either {
        logger.info { "completeSubtask()" }
        this@completeSubtask.copy(
            subtasks = this@completeSubtask.subtasks
                .replaceTask(
                    subtask.copy(
                        completedOn = Clock.System.now().toOption(),
                        fileInfo = subtask.fileInfo.copy(original = subtask.completeAsMarkdown())
                    )
                )
        )
    }

    /**
     * Replaces a Task within the Task list, based on the Tasks UUID.
     *
     * @receiver List of Tasks to process
     * @param newTask The updated Task
     * @return List of Tasks with the Task with the same UUID replaced
     */
    fun List<Task>.replaceTask(newTask: Task) : List<Task> {
        logger.info { "replaceTask()" }
        return this.map {
            if (it.id == newTask.id) {
                newTask
            } else {
                it
            }
        }
    }

    fun List<Task>.maybeAddDataviewValues(settings: MinionSettings, dataview: Map<DataviewField, DataviewValue>)
    : Either<MinionError, List<Task>> = either {
        logger.debug { "maybeAddDataviewValues\n$settings\n$dataview" }
        if (settings.pageTaskFields.isEmpty()) {
            logger.debug { "Empty settings" }
            this@maybeAddDataviewValues
        } else {
            this@maybeAddDataviewValues
                .map { task ->
                    task.copy(
                        tags = task.tags.maybeAddDataviewTags(settings, dataview).bind(),
                    )
                }
        }
    }

    fun Set<Tag>.maybeAddDataviewTags(settings: MinionSettings, dataview: Map<DataviewField, DataviewValue>)
    : Either<MinionError, Set<Tag>> = either {
        logger.debug { "maybeAddDataviewTags" }
        settings
            .pageTaskFields
            .filter { it.type == PageTaskFieldType.TAG }
            .map { it.field }
            .let { fields ->
                dataview.filter { fields.contains(it.key) }
            }
            .map { Tag(it.value.v.drop(1), type = TagType.PAGE) } // value contains #, drop it
            .let {
                logger.debug { "Adding $it to tag set" }
                this@maybeAddDataviewTags.plus(it)
            }
    }
}}
