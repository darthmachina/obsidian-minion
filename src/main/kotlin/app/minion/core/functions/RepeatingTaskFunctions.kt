package app.minion.core.functions

import app.minion.core.model.DateTime
import arrow.core.Option
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import mu.KotlinLogging

private val logger = KotlinLogging.logger("RepeatingTaskFunctions")

interface RepeatingTaskFunctions { companion object {
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
