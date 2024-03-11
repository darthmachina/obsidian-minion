package app.minion.shell.view.codeblock

import app.minion.core.MinionError
import app.minion.core.model.FileData
import app.minion.core.model.Filename
import app.minion.core.model.Tag
import app.minion.core.store.State
import arrow.core.Either
import arrow.core.flatten
import arrow.core.raise.either

interface CodeBlockPageFunctions { companion object {
    fun State.applyCodeBlockConfig(config: CodeBlockConfig) : Either<MinionError, List<FileData>> = either {

        emptyList()
    }

    fun Set<Filename>.applyInclude(state: State, config: CodeBlockConfig) : Either<MinionError, Set<Filename>> = either {
        this@applyInclude
    }

    fun Set<Filename>.applyIncludeTags(state: State, config: CodeBlockConfig) : Either<MinionError, Set<Filename>> = either {
        if (config.include.tags.isNotEmpty()) {
            this@applyIncludeTags
                .minus(state.tagCache.findFilesMatchingTags(config.include.tags.toSet()).bind())
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
}}