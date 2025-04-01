package app.minion.core.functions

import kotlin.uuid.Uuid

import app.minion.core.MinionError
import app.minion.core.functions.DateTimeFunctions.Companion.addDays
import app.minion.core.functions.DateTimeFunctions.Companion.daysDifference
import app.minion.core.functions.DateTimeFunctions.Companion.isInPast
import app.minion.core.model.DateTime
import app.minion.core.model.RepeatInfo
import app.minion.core.model.RepeatSpan
import app.minion.core.model.Task
import arrow.core.*
import arrow.core.raise.either
import kotlinx.datetime.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger("RepeatingTaskFunctions")

interface RepeatingTaskFunctions { companion object {
    fun Task.maybeRepeat() : Option<Task> {
        return this.dueDate
            .map { dueDate ->
                this.repeatInfo
                    .map { repeatInfo ->
                        dueDate.calculateNextRepeat(repeatInfo)
                            .map { nextRepeat ->
                                this.copy(
                                    id = Uuid.random(),
                                    dueDate = nextRepeat.toOption(),
                                    hideUntil = this.hideUntil.calculateNextHideUntil(dueDate, nextRepeat)
                                )
                            }
                            .getOrNone()
                    }
            }
            .flatten()
            .flatten()
    }

    fun DateTime.calculateNextRepeat(repeatInfo: RepeatInfo, instant: Instant = Clock.System.now()) : Either<MinionError, DateTime> = either {
        logger.info { "calculateNextRepeat: $this, $repeatInfo"}
        val fromDate = if (repeatInfo.afterComplete) instant.toLocalDateTime(TimeZone.of("America/Chicago")).date else this@calculateNextRepeat.date
        logger.info { "- fromDate: $fromDate" }
        val repeatDate = when (repeatInfo.span) {
            RepeatSpan.DAILY -> this@calculateNextRepeat.copy(date = fromDate.plus(repeatInfo.value, DateTimeUnit.DAY))
            RepeatSpan.WEEKDAY -> this@calculateNextRepeat.copy(date = fromDate.calculatedNextWeekday(repeatInfo.value))
            RepeatSpan.WEEKLY -> this@calculateNextRepeat.copy(date = fromDate.plus(repeatInfo.value, DateTimeUnit.WEEK))
            RepeatSpan.MONTHLY -> this@calculateNextRepeat.copy(date = fromDate.plus(repeatInfo.value, DateTimeUnit.MONTH))
            RepeatSpan.YEARLY -> this@calculateNextRepeat.copy(date = fromDate.plus(repeatInfo.value, DateTimeUnit.YEAR))
            else -> {
                raise(MinionError.RepeatDateError("${repeatInfo.span} not handled yet"))
            }
        }
        logger.info { "- repeatDate: $repeatDate" }
        if (repeatDate.isInPast()) {
            repeatDate.calculateNextRepeat(repeatInfo, instant).bind()
        } else {
            repeatDate
        }
    }

    fun LocalDate.calculatedNextWeekday(numberOfDays: Int) : LocalDate {
        // Add single days while decrementing 'numberOfDays', adding two more days if the current day is Sat to become Mon
        var currentDate = this
        for(i in numberOfDays downTo 0) {
            currentDate = currentDate.plus(1, DateTimeUnit.DAY)
            if (currentDate.dayOfWeek == DayOfWeek.SATURDAY) {
                currentDate = currentDate.plus(2, DateTimeUnit.DAY)
            }
        }
        return currentDate
    }

    fun Option<DateTime>.calculateNextHideUntil(dueDate: DateTime, newDueDate: DateTime) : Option<DateTime> {
        return this.map {
            it.addDays(dueDate.daysDifference(newDueDate))
        }
    }
}}
