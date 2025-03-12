package app.minion.core.store

import MarkdownView
import MinionPlugin
import app.minion.core.MinionError
import app.minion.core.model.DataviewField
import app.minion.core.model.DataviewValue
import app.minion.core.model.FileData
import app.minion.core.model.Filename
import app.minion.core.model.Tag
import app.minion.core.model.Task
import app.minion.core.store.StateFunctions.Companion.findTaskForSourceAndLine
import app.minion.shell.view.modal.KanbanStatusSelectModal
import arrow.core.Either
import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.toOption
import mu.KotlinLogging

private val logger = KotlinLogging.logger("StateFunctions")

interface StateFunctions { companion object {
    fun Map<Filename, FileData>.upsertData(fileData: FileData) : Map<Filename, FileData> {
        return if (containsKey(fileData.name)) {
            mapValues { entry ->
                if (entry.key == fileData.name) {
                    fileData
                } else {
                    entry.value
                }
            }
        } else {
            plus(fileData.name to fileData)
        }
    }

    fun List<Task>.replaceTasks(fileData: FileData) : List<Task> {
        return this
            .removeFor(fileData.name)
            .plus(fileData.tasks)
    }

    fun List<Task>.removeFor(name: Filename) : List<Task> {
        return this.filter { it.fileInfo.file != name }
    }

    fun FileData.updateTagCache(tagCache: Map<Tag, Set<Filename>>) : Map<Tag, Set<Filename>> {
        return tagCache
            .removeFor(this.name)
            .plus(tags
                .associateWith { name }
                .mapValues { entry ->
                    setOf(entry.value).plus(tagCache[entry.key] ?: emptySet())
                }
            )
    }

    fun Map<Tag, Set<Filename>>.removeFor(name: Filename) : Map<Tag, Set<Filename>> {
        return this.mapValues { it.value.minus(name) }
    }

    fun FileData.updateDataviewCache(dataviewCache: Map<Pair<DataviewField, DataviewValue>, Set<Filename>>)
    : Map<Pair<DataviewField, DataviewValue>, Set<Filename>> {
        return dataviewCache
            .removeFor(this.name)
            .plus(dataview
                .map { entry -> Pair(entry.key, entry.value) }
                .associateWith { name }
                .mapValues { entry ->
                    setOf(entry.value).plus(dataviewCache[entry.key] ?: emptySet())
                }
            )
    }

    fun Map<Pair<DataviewField, DataviewValue>, Set<Filename>>.removeFor(name: Filename)
    : Map<Pair<DataviewField, DataviewValue>, Set<Filename>> {
        return this.mapValues { it.value.minus(name) }
    }

    fun FileData.updateBacklinkCache(backlinkCache: Map<Filename, Set<Filename>>) : Map<Filename, Set<Filename>> {
        return backlinkCache
            .removeFor(this.name)
            .plus(outLinks
                .associateWith { name }
                .mapValues { entry ->
                    logger.info { "${entry.key} to ${entry.value}" }
                    setOf(entry.value).plus(backlinkCache[entry.key] ?: emptySet())
                }
            )
    }

    fun Map<Filename, Set<Filename>>.removeFor(name: Filename) : Map<Filename, Set<Filename>> {
        return this.mapValues { it.value.minus(name) }
    }

    fun MinionStore.findTaskAtCursor() : Either<MinionError, Task> = either {
        this@findTaskAtCursor.store.state.plugin.map { plugin ->
            plugin.app.workspace.activeLeaf.toOption().map { leaf ->
                if (leaf.view is MarkdownView) {
                    val line = (leaf.view as MarkdownView).editor.getCursor("head").line.toInt()
                    val file = (leaf.view as MarkdownView).file.basename.removeSuffix(".md")

                    logger.debug { "Working on $file:$line" }
                    store.store.state.findTaskForSourceAndLine(Filename(file), line).bind()
                }
            }
        }
        .onNone {
            logger.warn { "No Plugin defined" }
            raise(MinionError.TaskNotFoundError("No task found"))
        }
    }

    fun State.findTaskForSourceAndLine(source: Filename, line: Int) : Either<MinionError.TaskNotFoundError, Task> = either {
        this@findTaskForSourceAndLine.files
            .get(source)
            .toOption()
            .map { fileData -> fileData.tasks.filter { task -> task.fileInfo.line == line } }
            .map { taskList ->
                if (taskList.isEmpty()) {
                    raise(MinionError.TaskNotFoundError("No Task found at ${source.v}:$line"))
                }
                // taskList.size > 1 should be impossible, check for it?
                taskList[0]
            }
            .getOrElse {
                raise(MinionError.TaskNotFoundError("No Task found at ${source.v}:$line"))
            }
    }
}}
