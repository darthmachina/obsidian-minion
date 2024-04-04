package app.minion.core.model

import app.minion.core.MinionError
import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.toOption

enum class KanbanStatus(val tag: String, val display: String) {
    ICEBOX("icebox", "Icebox"),
    BACKLOG("backlog", "Backlog"),
    NEXT_MONTH("nextmonth", "Next Month"),
    THIS_MONTH("thismonth", "This Month"),
    NEXT_WEEK("nextweek", "Next Week"),
    THIS_WEEK("thisweek", "This Week"),
    IN_PROGRESS("inprogress", "In Progress"),
    SCHEDULED("scheduled", "Scheduled"),
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
