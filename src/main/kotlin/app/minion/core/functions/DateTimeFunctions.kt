package app.minion.core.functions

import app.minion.core.model.DateTime
import arrow.core.getOrElse
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import mu.KotlinLogging

private val logger = KotlinLogging.logger("DateTimeFunctions")

interface DateTimeFunctions { companion object {
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

}}
