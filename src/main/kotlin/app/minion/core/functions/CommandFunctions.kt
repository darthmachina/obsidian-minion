package app.minion.core.functions

import MarkdownView
import app.minion.core.model.Filename
import app.minion.core.store.MinionStore
import app.minion.core.store.StateFunctions.Companion.findTaskForSourceAndLine
import app.minion.shell.view.modal.KanbanStatusSelectModal
import arrow.core.toOption
import mu.KotlinLogging

private val logger = KotlinLogging.logger("CommandFunctions")

interface CommandFunctions { companion object {
    fun changeTaskStatus(store: MinionStore) {
        store.store.state.plugin
            .map { plugin ->
                plugin.app.workspace.activeLeaf.toOption().map { leaf ->
                    if (leaf.view is MarkdownView) {
                        val line = (leaf.view as MarkdownView).editor.getCursor("head").line.toInt()
                        val file = (leaf.view as MarkdownView).file.basename.removeSuffix(".md")

                        logger.debug { "Working on $file:$line" }
                        store.store.state.findTaskForSourceAndLine(Filename(file), line).map { task ->
                            KanbanStatusSelectModal(store, task, plugin.app).open()
                        }
                    }
                }
            }
            .onNone {
                logger.warn { "No Plugin defined" }
            }
    }
}}