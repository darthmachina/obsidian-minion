package app.minion.core.functions

import app.minion.core.MinionError
import app.minion.shell.functions.WIKILINK_EMBED_REGEX
import arrow.core.Either
import arrow.core.flatten
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.toOption

interface ImageFunctions { companion object {
    fun String.getImageName() : Either<MinionError, String> = either {
        WIKILINK_EMBED_REGEX
            .find(this@getImageName)
            .toOption()
            .map {
                it.groups[1].toOption().map { it.value }
            }
            .flatten()
            .getOrElse {
                raise(MinionError.ImageNotFoundError("Error parsing '${this@getImageName}' for image"))
            }

    }
}}