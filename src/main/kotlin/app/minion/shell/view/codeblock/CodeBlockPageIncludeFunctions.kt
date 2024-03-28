package app.minion.shell.view.codeblock

import app.minion.core.MinionError
import app.minion.core.functions.PageFilterFunctions.Companion.filterByAnyDataview
import app.minion.core.functions.PageFilterFunctions.Companion.filterByAnyTag
import app.minion.core.functions.PageFilterFunctions.Companion.filterByDataview
import app.minion.core.functions.PageFilterFunctions.Companion.filterByTags
import app.minion.core.model.DataviewField
import app.minion.core.model.DataviewValue
import app.minion.core.model.FileData
import app.minion.core.model.Tag
import arrow.core.Either
import arrow.core.raise.either

interface CodeBlockPageIncludeFunctions { companion object {
    fun List<FileData>.applyInclude(include: IncludeExcludeOptions) : Either<MinionError, List<FileData>> = either {
        if (include.and.isNotEmpty()) {
            this@applyInclude.applyIncludeAnd(include.and).bind()
        } else if (include.or.isNotEmpty()) {
            this@applyInclude.applyIncludeOr(include.or).bind()
        } else {
            this@applyInclude
                .applyIncludeTagsAnd(include).bind()
                .applyIncludeDataviewAnd(include).bind()
        }
    }

    fun List<FileData>.applyIncludeAnd(includeList: List<IncludeExcludeOptions>) : Either<MinionError, List<FileData>> = either {
        var filteredPages = this@applyIncludeAnd
        includeList.forEach { include ->
            filteredPages = if (include.and.isNotEmpty()) {
                filteredPages.applyIncludeAnd(include.and).bind()
            } else if (include.or.isNotEmpty()) {
                filteredPages.applyIncludeOr(include.or).bind()
            } else {
                filteredPages
                    .applyIncludeTagsAnd(include).bind()
                    .applyIncludeDataviewAnd(include).bind()
            }
        }
        filteredPages
    }

    fun List<FileData>.applyIncludeOr(includeList: List<IncludeExcludeOptions>) : Either<MinionError, List<FileData>> = either {
        var filteredPages = this@applyIncludeOr
        includeList.forEach { include ->
            filteredPages = if (include.and.isNotEmpty()) {
                filteredPages.applyIncludeAnd(include.and).bind()
            } else if (include.or.isNotEmpty()) {
                filteredPages.applyIncludeOr(include.or).bind()
            } else {
                filteredPages
                    .applyIncludeTagsOr(include).bind()
                    .applyIncludeDataviewOr(include).bind()
            }
        }
        filteredPages
    }

    fun List<FileData>.applyIncludeTagsAnd(include: IncludeExcludeOptions) : Either<MinionError, List<FileData>> = either {
        if (include.tags.isNotEmpty()) {
            this@applyIncludeTagsAnd.filterByTags(include.tags.map { Tag(it) })
        } else {
            this@applyIncludeTagsAnd
        }
    }

    fun List<FileData>.applyIncludeTagsOr(include: IncludeExcludeOptions) : Either<MinionError, List<FileData>> = either {
        if (include.tags.isNotEmpty()) {
            this@applyIncludeTagsOr.filterByAnyTag(include.tags.map { Tag(it) })
        } else {
            this@applyIncludeTagsOr
        }
    }

    fun List<FileData>.applyIncludeDataviewAnd(include: IncludeExcludeOptions) : Either<MinionError, List<FileData>> = either {
        if (include.dataview.isNotEmpty()) {
            this@applyIncludeDataviewAnd.filterByDataview(include.dataview.toDataviewPair().bind())
        } else {
            this@applyIncludeDataviewAnd
        }
    }

    fun List<FileData>.applyIncludeDataviewOr(include: IncludeExcludeOptions) : Either<MinionError, List<FileData>> = either {
        if (include.dataview.isNotEmpty()) {
            this@applyIncludeDataviewOr.filterByAnyDataview(include.dataview.toDataviewPair().bind())
        } else {
            this@applyIncludeDataviewOr
        }
    }

    fun List<String>.toDataviewPair() : Either<MinionError, List<Pair<DataviewField, DataviewValue>>> = either {
        this@toDataviewPair
            .map { dataviewConfig ->
                dataviewConfig
                    .split("::")
                    .let {
                        if (it.size != 2) {
                            raise(MinionError.ConfigError("Incorrect Dataview specification: $it"))
                        }
                        Pair(DataviewField(it[0].trim()), DataviewValue(it[1].trim()))
                    }
            }
    }
}}
