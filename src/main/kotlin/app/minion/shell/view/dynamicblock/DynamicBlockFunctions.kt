package app.minion.shell.view.dynamicblock

import app.minion.core.model.Content
import com.github.h0tk3y.betterParse.utils.Tuple2

interface DynamicBlockFunctions { companion object {
    fun String.processBlock(currentLine: Int) : String {
        // Go up in text line by line until finding one that starts with <!--+BEGIN
        // Go down in text line by line under finding one that starts with <!--+END
        // Save start and end lines
        // Parse query
        // Remove all lines between start and end
        // Process query into output
        // Add lines between start and end lines
    }

    fun String.findBegin(fromLine: Int) : Tuple2<Int, String> {

    }

    fun String.findEnd(fromLine: Int) : Tuple2<Int, String> {

    }

    /**
     * TODO Replace return with query object
     */
    fun String.parseQuery() : String {

    }

    fun String.removeBlockLine(begin: Int, end: Int) : Content {

    }

    fun String.addBlockResults(begin: Int) : Content {

    }
}}
