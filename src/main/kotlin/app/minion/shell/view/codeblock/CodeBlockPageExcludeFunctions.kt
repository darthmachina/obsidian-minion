package app.minion.shell.view.codeblock

import app.minion.core.MinionError
import app.minion.core.functions.PageFilterFunctions.Companion.excludeByDataview
import app.minion.core.model.FileData
import app.minion.shell.view.codeblock.CodeBlockPageIncludeFunctions.Companion.toDataviewPair
import arrow.core.Either
import arrow.core.raise.either
import mu.KotlinLogging

private val logger = KotlinLogging.logger("CodeBlockPageExcludeFunctions")

interface CodeBlockPageExcludeFunctions { companion object {
    fun List<FileData>.applyExclude(exclude: IncludeExcludeOptions) : Either<MinionError, List<FileData>> = either {
        logger.debug { "applyExclude: $exclude" }
        if (exclude.and.isNotEmpty()) {
            this@applyExclude.applyExcludeAnd(exclude.and).bind()
        } else if (exclude.or.isNotEmpty()) {
            this@applyExclude.applyExcludeOr(exclude.or).bind()
        } else {
            this@applyExclude
                .applyExcludeDataviewAnd(exclude).bind()
        }
    }

    fun List<FileData>.applyExcludeAnd(excludeList: List<IncludeExcludeOptions>)
    : Either<MinionError, List<FileData>> = either {
        var filteredPages = this@applyExcludeAnd
        excludeList.forEach { exclude ->
            filteredPages = if (exclude.and.isNotEmpty()) {
                filteredPages.applyExcludeAnd(exclude.and).bind()
            } else if (exclude.or.isNotEmpty()) {
                filteredPages.applyExcludeOr(exclude.or).bind()
            } else {
                filteredPages
                    .applyExcludeDataviewAnd(exclude).bind()
            }
        }
        filteredPages
    }

    fun List<FileData>.applyExcludeOr(excludeList: List<IncludeExcludeOptions>)
    : Either<MinionError, List<FileData>> = either {
        this@applyExcludeOr
    }

    fun List<FileData>.applyExcludeDataviewAnd(exclude: IncludeExcludeOptions) : Either<MinionError, List<FileData>> = either {
        if (exclude.dataview.isNotEmpty()) {
            this@applyExcludeDataviewAnd.excludeByDataview(exclude.dataview.toDataviewPair().bind())
        } else {
            this@applyExcludeDataviewAnd
        }
    }
}}