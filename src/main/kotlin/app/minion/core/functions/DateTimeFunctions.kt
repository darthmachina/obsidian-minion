package app.minion.core.functions

import app.minion.core.MinionError
import app.minion.core.functions.DateTimeFunctions.Companion.toLocalDateTime
import app.minion.core.model.DateTime
import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.some
import arrow.core.toOption
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import mu.KotlinLogging
import kotlin.math.log

private val logger = KotlinLogging.logger("DateTimeFunctions")

interface DateTimeFunctions { companion object {
    fun parseDateTime(value: String) : Either<MinionError, DateTime> = either {
        logger.debug { "parseDateTime(): $value" }
        val dateTimeSplit = value.split("T")
        val dateSplit = dateTimeSplit[0].split("-").map { it.toInt() }
        val maybeTimeSplit = if (dateTimeSplit.size == 2) {
            dateTimeSplit[1].split(":").map { it.split(".")[0].toInt() }.toOption()
        } else { None }

        maybeTimeSplit
            .map {
                DateTime(LocalDate(dateSplit[0], dateSplit[1], dateSplit[2]), LocalTime(it[0], it[1], 0).toOption())
            }
            .getOrElse {
                DateTime(LocalDate(dateSplit[0], dateSplit[1], dateSplit[2]), None)
            }
    }

    fun DateTime.asString() : String {
        val month = this.date.monthNumber.toString().padStart(2, '0')
        val day = this.date.dayOfMonth.toString().padStart(2, '0')
        return "${this.date.year}-${month}-${day}${this.time.asString()}"
    }

    fun Option<LocalTime>.asString() : String {
        return this.map {
            val hour = it.hour.toString().padStart(2, '0')
            val minute = it.minute.toString().padStart(2, '0')
            " ${hour}:${minute}"
        }.getOrElse { "" }
    }

    fun DateTime.toLocalDateTime() : LocalDateTime {
        return LocalDateTime(this.date, this.time.getOrElse { LocalTime(hour = 23, minute = 59) })
    }

    fun LocalDateTime.toDateTime() : DateTime {
        return DateTime(this.date, this.time.some())
    }

    fun DateTime.daysDifference() : Int {
        val hoursDifference = Clock.System.now()
            .minus(this.toLocalDateTime().toInstant(TimeZone.currentSystemDefault()), DateTimeUnit.HOUR)
        return hoursDifference.toInt() / 24
    }

    fun DateTime.daysDifference(other: DateTime) : Int {
        val hoursDifference = other
            .toLocalDateTime()
            .toInstant(TimeZone.currentSystemDefault())
            .minus(this.toLocalDateTime().toInstant(TimeZone.currentSystemDefault()), DateTimeUnit.HOUR)
        return hoursDifference.toInt() / 24
    }

    fun DateTime.addDays(days: Int) : DateTime {
        return this
            .toLocalDateTime()
            .toInstant(TimeZone.currentSystemDefault())
            .plus(days, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .let {
                DateTime(it.date, this.time)
            }
    }

    fun DateTime.isInPast() : Boolean {
        val hoursDifference = Clock.System.now()
            .minus(this.toLocalDateTime().toInstant(TimeZone.currentSystemDefault()), DateTimeUnit.HOUR)
        return hoursDifference > 0
    }

    fun DateTime.isTodayOrOverdue() : Boolean {
        return isToday() || isInPast()
    }


    fun DateTime.isToday() : Boolean {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return today.year == this.date.year
                && today.monthNumber == this.date.monthNumber
                && today.dayOfMonth == this.date.dayOfMonth
    }

    /** Returns true if the Date is in the range tomorrow -> 7 days from not, false otherwise */
    fun DateTime.isUpcoming() : Boolean {
        val tomorrow = Clock.System.now()
            .plus(1, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .toDateTime()
            .copy(time = LocalTime(0, 0, 0).some())
            .toLocalDateTime()
            .toInstant(TimeZone.currentSystemDefault())
        val sevenDays = Clock.System.now()
            .plus(7, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .toDateTime()
            .copy(time = LocalTime(23, 59, 59).some())
            .toLocalDateTime()
            .toInstant(TimeZone.currentSystemDefault())
        return this
            .toLocalDateTime()
            .toInstant(TimeZone.currentSystemDefault()) in tomorrow..sevenDays

    }
}}
