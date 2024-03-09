package app.minion.core.functions

import app.minion.core.functions.MarkdownConversionFunctions.Companion.toMarkdown
import app.minion.core.model.DateTime
import app.minion.core.model.RepeatInfo
import app.minion.core.model.RepeatSpan
import app.minion.util.test.TaskFactory
import arrow.core.None
import arrow.core.toOption
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equals.shouldBeEqual
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

class MarkdownConversionFunctionsTest : StringSpec({
    "Pair.toMarkdown creates two lines for a repeating task" {
        val expectedRepeatInfo = RepeatInfo(1, RepeatSpan.DAILY, false)
        val repeatTask = TaskFactory.createBasicTask().copy(
            dueDate = DateTime(LocalDate(2050, 1, 3)).toOption(),
            repeatInfo = expectedRepeatInfo.toOption(),
        )
        val oldTask = repeatTask.copy(
            dueDate = DateTime(LocalDate(2050, 1, 2)).toOption(),
            completedOn = Instant.fromEpochMilliseconds(0).toOption(),
            completed = true,
            repeatInfo = None
        )

        val taskPair = Pair(oldTask, repeatTask.toOption())
        val actual = taskPair.toMarkdown()

        actual shouldBeEqual """- [x] Test task #task [due:: 2050-01-02]  [c:: 0]
- [ ] Test task #task [due:: 2050-01-03]  [repeat:: daily: 1]
""".trimIndent()
    }

    "DateTime.toMarkdown creates correct output for a date" {
        val date = DateTime(LocalDate(2024, 1, 2))

        val actual = date.toMarkdown("test")
        actual shouldBeEqual "[test:: 2024-01-02]"
    }

    "DateTime.toMarkdown creates correct output for a date and time" {
        val dateTime = DateTime(LocalDate(2024, 1, 2), LocalTime(3, 10, 0).toOption())

        val actual = dateTime.toMarkdown("test")
        actual shouldBeEqual "[test:: 2024-01-02 03:10]"
    }
})