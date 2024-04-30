package app.minion.core.functions

import app.minion.core.functions.DateTimeFunctions.Companion.addDays
import app.minion.core.functions.DateTimeFunctions.Companion.asString
import app.minion.core.functions.DateTimeFunctions.Companion.isInPast
import app.minion.core.functions.DateTimeFunctions.Companion.isUpcoming
import app.minion.core.functions.DateTimeFunctions.Companion.toDateTime
import app.minion.core.model.DateTime
import arrow.core.toOption
import io.kotest.assertions.arrow.core.shouldBeNone
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.arrow.core.shouldBeSome
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.equals.shouldBeEqual
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

class DateTimeFunctionsTest : StringSpec({
    "parseDateTime parses date" {
        val date = "2024-01-02"

        val actualEither = DateTimeFunctions.parseDateTime(date)
        val actual = actualEither.shouldBeRight()

        actual.date.year shouldBeEqual 2024
        actual.date.monthNumber shouldBeEqual 1
        actual.date.dayOfMonth shouldBeEqual 2
        actual.time.shouldBeNone()
    }

    "parseDateTime parses date and time" {
        val dateTime = "2024-01-02T03:10"

        val actualEither = DateTimeFunctions.parseDateTime(dateTime)
        val actual = actualEither.shouldBeRight()

        actual.date.year shouldBeEqual 2024
        actual.date.monthNumber shouldBeEqual 1
        actual.date.dayOfMonth shouldBeEqual 2
        val actualTime = actual.time.shouldBeSome()
        actualTime.hour shouldBeEqual 3
        actualTime.minute shouldBeEqual 10
    }

    "DateTime.asString create correct string for date" {
        val date = DateTime(LocalDate(2024, 1, 2))

        val actual = date.asString()
        actual shouldBeEqual "2024-01-02"
    }

    "DateTime.asString create correct string for date and time" {
        val dateTime = DateTime(LocalDate(2024, 1, 2), LocalTime(3, 10, 0).toOption())

        val actual = dateTime.asString()
        actual shouldBeEqual "2024-01-02 03:10"
    }

    "DateTime.addDays produces the correct result" {
        val date = DateTime(LocalDate(2024, 1, 2))

        val actual = date.addDays(3)
        actual.date.year shouldBeEqual 2024
        actual.date.monthNumber shouldBeEqual 1
        actual.date.dayOfMonth shouldBeEqual 5
        actual.time.shouldBeNone()
    }

    "DateTime.isInPast return true for date in past" {
        val date = DateTime(LocalDate(2024, 1, 2))

        val actual = date.isInPast()
        actual.shouldBeTrue()
    }

    "DateTime.isInPast returns false for date in the future" {
        val date = DateTime(LocalDate(2099, 1, 2))

        val actual = date.isInPast()
        actual.shouldBeFalse()
    }

    "isUpcoming returns false for today" {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toDateTime()

        val actual = today.isUpcoming()
        actual.shouldBeFalse()
    }

    "isUpcoming returns true for tomorrow" {
        val today = Clock.System.now()
            .plus(1, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
            .toLocalDateTime(TimeZone.currentSystemDefault()).toDateTime()

        val actual = today.isUpcoming()
        actual.shouldBeTrue()
    }

    "isUpcoming returns true for seven days" {
        val today = Clock.System.now()
            .plus(7, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
            .toLocalDateTime(TimeZone.currentSystemDefault()).toDateTime()

        val actual = today.isUpcoming()
        actual.shouldBeTrue()
    }

    "isUpcoming returns false for eight days" {
        val today = Clock.System.now()
            .plus(8, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
            .toLocalDateTime(TimeZone.currentSystemDefault()).toDateTime()

        val actual = today.isUpcoming()
        actual.shouldBeFalse()
    }
})
