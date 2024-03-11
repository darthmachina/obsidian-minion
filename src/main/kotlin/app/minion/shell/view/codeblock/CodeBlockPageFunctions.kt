package app.minion.shell.view.codeblock

import app.minion.core.MinionError
import app.minion.core.model.FileData
import app.minion.core.model.Filename
import app.minion.core.model.Tag
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
}}
