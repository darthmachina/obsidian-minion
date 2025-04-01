package app.minion.core.functions

import app.minion.core.MinionError
import arrow.core.Either
import arrow.core.flatten
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.toOption
import mu.KotlinLogging

private val logger = KotlinLogging.logger("ImageFunctions")

interface ImageFunctions { companion object {
    fun String.getImageName() : Either<MinionError, String> = either {
        listOf(WIKILINK_EMBED_REGEX, WIKILINK_REGEX)
            .map { regex ->
                logger.debug { "Checking ${this@getImageName} against $regex" }
                val result = regex.find(this@getImageName).toOption()
                logger.debug { "result : $result" }
                result
            }
            .map { result -> result.flatMap { it.groups[1].toOption() } }
            .map { group -> group.map { it.value } }
            .mapNotNull { name -> name.getOrNull() }
            .let { list ->
                if (list.isEmpty()) {
                    raise(MinionError.ImageNotFoundError("Error parsing '${this@getImageName}' for image"))
                } else {
                    list.first().split("|")[0]
                }
            }
    }
}}
