package app.minion.shell.view.dynamicblock

import app.minion.core.MinionError
import app.minion.core.functions.MarkdownConversionFunctions.Companion.toMarkdown
import app.minion.core.functions.TaskFilterFunctions.Companion.filterByTodayOrOverdue
import app.minion.core.model.Content
import app.minion.core.model.Tag
import app.minion.core.store.State
import arrow.core.*
import arrow.core.raise.either
import com.github.h0tk3y.betterParse.utils.Tuple2

const val BEGIN_MARKER = "<!--+BEGIN minion"
const val END_MARKER = "<!--+END"

interface DynamicBlockFunctions { companion object {
    fun String.processBlock(state: State, currentLine: Int) : Either<MinionError, String> = either {
        // Go up in text line by line until finding one that starts with <!--+BEGIN
        // Go down in text line by line under finding one that starts with <!--+END
        // Save start and end lines
        // Parse query
        // Remove all lines between start and end
        // Process query into output
        // Add lines between start and end lines
        val splitContents = this@processBlock.split("\n")
        val beginLine = splitContents.findBegin(currentLine).bind()
        val endLine = splitContents.findEnd(currentLine).bind()
        splitContents.addBlockResults(state, beginLine.t1).bind()
    }

    fun List<String>.findBegin(fromLine: Int) : Either<MinionError.NotInDynamicBlockError, Tuple2<Int, String>> = either {
        var index = fromLine
        var beginLine = -1
        while(index != 0) {
            if(this@findBegin[index].startsWith(BEGIN_MARKER)) {
                beginLine = index
                break
            }

            // If we find an END before a BEGIN, we aren't in a dynamic block
            if(this@findBegin[index].startsWith(END_MARKER)) {
                raise(MinionError.NotInDynamicBlockError("END marker found before BEGIN"))
            }

            index--
        }

        // If we found something return it else raise an error
        if(beginLine >= 0) {
            Tuple2(beginLine, this@findBegin[beginLine])
        } else {
            raise(MinionError.NotInDynamicBlockError("No <!--+BEGIN line found in correct spot"))
        }

    }

    fun List<String>.findEnd(fromLine: Int) : Either<MinionError.NotInDynamicBlockError, Tuple2<Int, String>> = either {
        var index = fromLine
        var endLine = -1
        while(index != this@findEnd.size) {
            if(this@findEnd[index].startsWith(END_MARKER)) {
                endLine = index
                break
            }

            // If we find a BEGIN before an END we aren't in a dynamic block
            if(this@findEnd[index].startsWith(BEGIN_MARKER)) {
                raise(MinionError.NotInDynamicBlockError("BEGIN marker found before END"))
            }

            index++
        }

        // If we found something return it else raise an error
        if(endLine >= 0) {
            Tuple2(endLine, this@findEnd[index])
        } else {
            raise(MinionError.NotInDynamicBlockError("No <!--+END line found in correct spot"))
        }
    }

    /**
     * TODO Replace return with query object
     */
    fun String.parseQuery() : Either<MinionError.QueryParseError, String> = either {
        // <!--+BEGIN minion :type task :display list :heading Scheduled :due today & overdue :include-->
        // Split on : and each one will be a parameter
        ""
    }

    fun String.removeBlockLine(begin: Int, end: Int) : Either<MinionError, String> = either {
        ""
    }

    fun List<String>.addBlockResults(state: State, beginLine: Int) : Either<MinionError, String> = either {
        this@addBlockResults
            .toMutableList()
            .let {
                it.add(
                    beginLine,
                    state.tasks
                        .filterByTodayOrOverdue()
                        .map { task ->
                            task.toMarkdown(listOf(Tag("task"))) // Don't want to output the task tag
                        }
                        .joinToString("\n"))
                it
            }
            .joinToString("\n")
    }
}}
