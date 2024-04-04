package app.minion.shell.view.codeblock

import app.minion.core.MinionError
import app.minion.core.model.DataviewField
import app.minion.core.model.DataviewValue
import app.minion.core.model.FileData
import app.minion.core.model.Filename
import app.minion.core.model.Tag
import app.minion.core.store.State
import app.minion.shell.view.codeblock.CodeBlockPageIncludeFunctions.Companion.applyInclude
import arrow.core.Either
import arrow.core.flatten
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.toOption
import mu.KotlinLogging

private val logger = KotlinLogging.logger("CodeBlockPageFunctions")

interface CodeBlockPageFunctions { companion object {
    fun State.applyCodeBlockConfig(config: CodeBlockConfig)
    : Either<MinionError, Map<String, List<FileData>>> = either {
        this@applyCodeBlockConfig
            .files
            .map { it.value }
            .applyInclude(config.include).bind()
            .sortedWith(compareBy { it.name.v })
            .applyGroupBy(config).bind()
    }

    fun Set<Filename>.getFileData(fileData: Map<Filename, FileData>) : Either<MinionError, Set<FileData>> = either {
        fileData
            .filterKeys {
                this@getFileData.contains(it)
            }
            .values
            .toSet()
    }

    fun Set<Filename>.applyIncludeWithCache(tagCache: Map<Tag, Set<Filename>>, config: CodeBlockConfig)
    : Either<MinionError, Set<Filename>> = either {
        this@applyIncludeWithCache
            .applyIncludeTags(tagCache, config).bind()
    }

    fun Set<Filename>.applyIncludeTags(tagCache: Map<Tag, Set<Filename>>, config: CodeBlockConfig)
    : Either<MinionError, Set<Filename>> = either {
        if (config.include.tags.isNotEmpty()) {
            tagCache
                .findFilesMatchingTags(config.include.tags.map { Tag(it) }.toSet()).bind()
                .let { tagFiles ->
                    this@applyIncludeTags
                        .filter {
                            tagFiles.contains(it)
                        }
                }
                .toSet()
       } else {
            this@applyIncludeTags
        }
    }

    fun Map<Tag, Set<Filename>>.findFilesMatchingTags(tags: Set<Tag>) : Either<MinionError, Set<Filename>> = either {
        this@findFilesMatchingTags
            .filterKeys { tag -> tags.contains(tag) }
            .values
            .flatten()
            .toSet()
    }

    fun List<FileData>.applySort(config: CodeBlockConfig) : Either<MinionError, List<FileData>> = either {
        if (config.sort.isNotEmpty()) {
            if (config.sort.contains(SORT_BY_EISENHOWER)) {
                raise(MinionError.ConfigError("$SORT_BY_EISENHOWER is not a valid page sort"))
            }

            // For now assume that any sort options are properties, as that is all to sort on for now.
            // Applies sorting in order defined in the sort list
            this@applySort.sortedWith(
                compareBy<FileData, DataviewValue?>(nullsLast()) {

                }
            )
        } else {
            sortedWith(compareBy { it.name.v })
        }
        this@applySort
    }

    fun List<FileData>.applyGroupBy(config: CodeBlockConfig)
    : Either<MinionError, Map<String, List<FileData>>> = either {
        if (config.groupBy == GroupByOptions.NONE) {
            mapOf(GROUP_BY_SINGLE to this@applyGroupBy)
        } else {
            when(config.groupBy) {
                GroupByOptions.dataview -> {
                    this@applyGroupBy
                        .applyGroupByForDataview(config).bind()
                        .mapKeys { it.key.v }
                }
                else -> raise(MinionError.ConfigError("${config.groupBy} not implement yet"))
            }
        }
    }

    /**
     * Groups a Set of FileData by the criteria specified in the config
     */
    fun List<FileData>.applyGroupByForDataview(config: CodeBlockConfig)
    : Either<MinionError, Map<DataviewValue, List<FileData>>> = either {
        if (config.groupBy == GroupByOptions.NONE) {
            raise(MinionError.ConfigError("applyGroupBy called with no groupBy specified"))
        }
        if (config.groupBy == GroupByOptions.dataview && config.groupByField.isEmpty()) {
            raise(MinionError.ConfigError("groupBy.dataview specified with no groupByField"))
        }
        // Collect valid group values
        // Group FileData into buckets by group values
        // Return map
        this@applyGroupByForDataview
            .groupBy { fileData ->
                fileData
                    .dataview[DataviewField(config.groupByField)]
                    .toOption()
                    .getOrElse {
                        DataviewValue(GROUP_BY_UNKNOWN)
                    }
            }
    }
}}
