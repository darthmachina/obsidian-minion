package app.minion.shell.functions

import app.minion.core.MinionError
import arrow.core.Either
import mu.KLogger

interface LogFunctions { companion object {
    fun <E, T> Either<E, T>.logLeft(logger: KLogger) : Either<E, T> {
        this
            .mapLeft {
                logger.warn { "Error: $it" }
            }
        return this
    }

    fun <E, T> Either<E, T>.log(logger: KLogger, output: String = "") : Either<E, T> {
        this
            .map {
                logger.debug { "$output $it" }
            }
        return this
    }
}}
