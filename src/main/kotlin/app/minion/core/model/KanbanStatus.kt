package app.minion.core.model

import app.minion.core.MinionError
import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.toOption

enum class KanbanStatus(val tag: String, val display: String) {
    ICEBOX("icebox", "Icebox"),
    BACKLOG("backlog", "Backlog"),
    NEXT("next", "Next"),
    PRIORITY("priority", "Priority"),
    SCHEDULED("scheduled", "Scheduled"),
    IN_PROGRESS("inprogress", "In Progress"),
    WAITING("waiting", "Waiting");

    companion object {
        fun findByTag(tag: String): Either<MinionError.TagNotFoundError, KanbanStatus> = either {
            entries
                .find { it.tag.equals(tag) }
                .toOption()
                .map { it }
                .getOrElse { raise(MinionError.TagNotFoundError("$tag not found in KanbanStatus")) }
        }
    }
}
