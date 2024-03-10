package app.minion.shell.functions

import ListItemCache
import MetadataCache
import app.minion.core.MinionError
import app.minion.core.functions.TaskParseFunctions.Companion.toTask
import app.minion.core.model.*
import app.minion.shell.functions.LogFunctions.Companion.logLeft
import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Tuple4
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.toOption
import mu.KotlinLogging

private val logger = KotlinLogging.logger("TaskReadFunctions")

interface TaskReadFunctions { companion object {
    fun String.processFileTasks(file: File, filename: Filename, metadataCache: MetadataCache) : Either<MinionError.VaultTaskReadError, List<Task>> = either {
        logger.debug { "processFileTasks()" }
        this@processFileTasks.split("\n")
            .let { contents ->
                metadataCache
                    .getCache(file.v)
                    .toOption()
                    .map {
                        logger.debug { "- converting listITems to list" }
                        it.listItems?.toList() ?: emptyList()
                    }
                    .map { listItemCache ->
                        listItemCache
                            .mapNotNull { listItem ->
                                contents[listItem.position.start.line.toInt()]
                                    .processLine(listItem, filename)
                                    .logLeft(logger)
                                    .getOrElse { None }
                                    .getOrNull()
                            }
                            .buildTaskTree()
                            .bind()
                    }
                    .getOrElse { emptyList() }
           }
    }

    fun String.processLine(item: ListItemCache, filename: Filename) : Either<MinionError.VaultTaskReadError, Option<ItemTuple>> = either {
        logger.debug { "processLine() : ${this@processLine}" }
        if (item.task == null && item.parent.toInt() > 0) {
            // Only process notes if they have a parent
            logger.debug { "Create tuple for Note" }
            createNoteTuple(this@processLine, filename, item.position.start.line.toInt(), item.parent.toInt())
                .bind()
                .toOption()
        } else if (this@processLine.contains("#task")) {
            logger.debug { "Create tuple for Task" }
            createTaskTuple(this@processLine, filename, item.position.start.line.toInt(), item.parent.toInt(), item.task?.isNotBlank() ?: false)
                .bind()
                .toOption()
        } else {
            logger.debug { "Ignoring as criteria isn't met" }
            None
        }
    }

    fun createTaskTuple(contents: String, filename: Filename, line: Int, parent: Int, completed: Boolean) : Either<MinionError.VaultTaskReadError, ItemTuple> = either {
        contents.toTask(filename, line, completed)
            .map {
                ItemTuple(
                    line,
                    it.toOption(),
                    None,
                    parent
                )
            }
            .mapLeft {
                MinionError.VaultTaskReadError(it.message, it.throwable, it.toOption())
            }.bind()
    }

    fun createNoteTuple(contents: String, filename: Filename, line: Int, parent: Int) : Either<MinionError.VaultTaskReadError, ItemTuple> = either {
        ItemTuple(
            line,
            None,
            Note(
                Content(contents.replace(noteSyntaxRegex, "")),
                ListItemFileInfo(filename, line, contents)
            ).toOption(),
            parent
        )
    }

    fun List<ItemTuple>.buildTaskTree() : Either<MinionError.VaultTaskReadError, List<Task>> = either {
        logger.debug { "buildTaskTree() root" }
        this@buildTaskTree
            .filter { item -> item.fourth < 0 && item.second.isSome() }     // Start with all top level tasks
            .mapNotNull { tuple ->
                tuple.second
                    .map { task ->
                        task.copy(
                            subtasks = this@buildTaskTree.buildTaskTree(task.fileInfo.line).bind(),
                            notes = this@buildTaskTree.buildNoteTree(task.fileInfo.line).bind()
                        )
                    }
                    .getOrNull()
            }
    }

    fun List<ItemTuple>.buildTaskTree(parent: Int) : Either<MinionError.VaultTaskReadError, List<Task>> = either {
        this@buildTaskTree
            .filter { it.fourth == parent && it.second.isSome() }
            .mapNotNull { tuple ->
                tuple.second
                    .map { task ->
                        task.copy(
                            subtasks = this@buildTaskTree.buildTaskTree(task.fileInfo.line).bind(),
                            notes = this@buildTaskTree.buildNoteTree(task.fileInfo.line).bind()
                        )
                    }
                    .getOrNull()
            }
    }

    fun List<ItemTuple>.buildNoteTree(parent: Int) : Either<MinionError.VaultTaskReadError, List<Note>> = either {
        this@buildNoteTree
            .filter { it.fourth == parent && it.third.isSome() }
            .mapNotNull { tuple ->
                tuple.third
                    .map { note ->
                        note.copy(subnotes = this@buildNoteTree.buildNoteTree(note.fileInfo.line).bind())
                    }
                    .getOrNull()
            }
    }
}}

typealias ItemTuple = Tuple4<Int, Option<Task>, Option<Note>, Int>
