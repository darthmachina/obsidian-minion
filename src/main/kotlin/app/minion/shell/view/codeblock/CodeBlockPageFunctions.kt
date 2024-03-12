package app.minion.shell.view.codeblock

import app.minion.core.MinionError
import app.minion.core.model.*
import app.minion.core.store.State
import arrow.core.Either
import arrow.core.flatten
import arrow.core.raise.either
import mu.KotlinLogging

private val logger = KotlinLogging.logger("CodeBlockPageFunctions")

interface CodeBlockPageFunctions { companion object {
    fun State.applyCodeBlockConfig(config: CodeBlockConfig) : Either<MinionError, List<FileData>> = either {
        this@applyCodeBlockConfig
            .files
            .keys
            .applyInclude(this@applyCodeBlockConfig.tagCache, config).bind()
            .getFileData(this@applyCodeBlockConfig.files).bind()
    }

    fun Set<Filename>.getFileData(fileData: Map<Filename, FileData>) : Either<MinionError, List<FileData>> = either {
        fileData
            .filterKeys {
                this@getFileData.contains(it)
            }
            .values
            .toList()
    }

    fun Set<Filename>.applyInclude(tagCache: Map<Tag, Set<Filename>>, config: CodeBlockConfig) : Either<MinionError, Set<Filename>> = either {
        this@applyInclude
            .applyIncludeTags(tagCache, config).bind()
    }

    fun Set<Filename>.applyIncludeTags(tagCache: Map<Tag, Set<Filename>>, config: CodeBlockConfig) : Either<MinionError, Set<Filename>> = either {
        if (config.include.tags.isNotEmpty()) {
            tagCache
                .findFilesMatchingTags(config.include.tags.toSet()).bind()
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
            .filterKeys { tag ->
                logger.debug { "Checking $tag: ${tags.contains(tag)}" }
                tags.contains(tag)
            }
            .values
            .flatten()
            .toSet()
    }

    /**
     * Groups a Set of FileData by the criteria specified in the config
     */
    fun Set<FileData>.applyGroupBy(config: CodeBlockConfig, dataviewFieldCache: Map<DataviewField, List<DataviewValue>>) : Either<MinionError, Map<DataviewValue, Set<FileData>>> = either {
        if (config.groupBy == GroupByOptions.NONE) {
            raise(MinionError.ConfigError("applyGroupBy called with no groupBy specified"))
        }
        if (config.groupBy == GroupByOptions.dataview && config.groupByField.isEmpty()) {
            raise(MinionError.ConfigError("groupBy.dataview specified with no groupByField"))
        }
        if (dataviewFieldCache[DataviewField(config.groupByField)] == null) {
            raise(MinionError.ConfigError("No values found for ${config.groupByField}"))
        }
        // Collect valid group values
        // Group FileData into buckets by group values
        // Return map
        this@applyGroupBy
            .filter { fileData ->
                fileData.dataview.containsKey(DataviewField(config.groupByField))
            }
            .groupBy { fileData ->
                fileData.dataview[DataviewField(config.groupByField)]!!
            }
            .mapValues { it.value.toSet() }
    }
}}
