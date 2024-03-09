package app.minion.core.functions

import app.minion.core.functions.RepeatingTaskFunctions.Companion.calculateNextRepeat
import app.minion.core.functions.RepeatingTaskFunctions.Companion.maybeRepeat
import app.minion.core.model.DateTime
import app.minion.core.model.RepeatInfo
import app.minion.core.model.RepeatSpan
import app.minion.util.test.TaskFactory
import arrow.core.toOption
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.arrow.core.shouldBeSome
import io.kotest.core.spec.style.StringSpec
import kotlinx.datetime.LocalDate

class RepeatingTaskFunctionsTest : StringSpec({
    "Task.maybeRepeat creates a valid Task when due and repeat are set" {
        val task = TaskFactory.createBasicTask().copy(
            dueDate = DateTime(LocalDate(2050, 1, 2)).toOption(),
            repeatInfo = RepeatInfo(1, RepeatSpan.DAILY, false).toOption()
        )

        val actualOption = task.maybeRepeat()
        val actual = actualOption.shouldBeSome()
        actual.dueDate.shouldBeSome(
            DateTime(LocalDate(2050, 1, 3))
        )
    }

    "DateTime.calculateNextRepeat creates the next 'daily' task" {
        val date = DateTime(LocalDate(2050, 1, 2))
        val repeatInfo = RepeatInfo(1, RepeatSpan.DAILY, false)

        val actualEither = date.calculateNextRepeat(repeatInfo)
        actualEither shouldBeRight DateTime(LocalDate(2050, 1, 3))
    }
})