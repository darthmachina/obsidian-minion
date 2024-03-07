package app.minion.core.functions

import app.minion.core.MinionError
import app.minion.core.model.DateTime
import arrow.core.*
import arrow.core.raise.either
import kotlinx.datetime.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger("DateTimeFunctions")

interface DateTimeFunctions { companion object {
    fun parseDateTime(value: String) : Either<MinionError, DateTime> = either {
        val dateTimeSplit = value.split(" ")
        val dateSplit = dateTimeSplit[0].split("-").map { it.toInt() }
        val maybeTimeSplit = if (dateTimeSplit.size == 2) { dateTimeSplit[1].split(":").map { it.toInt() }.toOption() } else { None }

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

    fun DateTime.daysDifference() : Int {
        val hoursDifference = Clock.System.now().minus(this.toLocalDateTime().toInstant(TimeZone.currentSystemDefault()), DateTimeUnit.HOUR)
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
        val hoursDifference = Clock.System.now().minus(this.toLocalDateTime().toInstant(TimeZone.currentSystemDefault()), DateTimeUnit.HOUR)
        return hoursDifference > 0
    }

}}
