package app.minion.core.model

import arrow.core.None
import arrow.core.Option
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

data class DateTime(
    val date: LocalDate,
    val time: Option<LocalTime> = None
) : Comparable<DateTime> {
    override fun compareTo(other: DateTime) = compareValuesBy(this, other, {it.date}, {it.time.getOrNull()})
}
