package app.minion.core.functions

import app.minion.core.MinionError
import app.minion.core.model.Tag
import app.minion.core.model.Task
import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.toOption
import mu.KotlinLogging

private val logger = KotlinLogging.logger("TaskTagFunctions")

interface TaskTagFunctions { companion object {
    fun Task.findTagWithPrefix(prefix: String) : Either<MinionError, Tag> = either {
        this@findTagWithPrefix.tags
            .find { it.v.startsWith(prefix) }
            .toOption()
            .getOrElse { raise(MinionError.TagPrefixNotFoundError("Tag with prefix '$prefix' not found")) }
    }

    fun Task.replaceTag(old: Tag, updated: Tag) : Either<MinionError, Task> = either {
        this@replaceTag.tags
            .map { tag ->
                if (tag == old) {
                    updated
                } else {
                    tag
                }
            }
            .toSet()
            .let { tags ->
                this@replaceTag.copy(tags = tags)
            }
    }

    fun Task.collectTags() : Set<Tag> {
        return this.subtasks
            .map { it.tags }
            .flatten()
            .toSet()
            .plus(this.tags)
    }

    fun Task.addTag(tag: Tag) : Either<MinionError, Task> = either {
        this@addTag
            .copy(tags = this@addTag.tags.plus(tag))
    }

    fun Set<Tag>.asString() : String {
        return this.joinToString(" ") { tag -> "#${tag.v}" }
    }
}}
