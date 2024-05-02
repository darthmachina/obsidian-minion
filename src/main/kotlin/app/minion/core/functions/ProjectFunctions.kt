package app.minion.core.functions

import app.minion.core.MinionError
import app.minion.core.model.todoist.Project
import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.toOption
import mu.KotlinLogging

private val logger = KotlinLogging.logger("ProjectFunctions")

interface ProjectFunctions { companion object {
    fun List<Project>.findInbox() : Either<MinionError, Project> = either {
        logger.debug { "findInbox projects:\n${this@findInbox}" }
        this@findInbox
            .find { it.name == "Inbox" }
            .toOption()
            .getOrElse {
                raise(MinionError.TodoistError("Cannot find Inbox project"))
            }
    }
}}