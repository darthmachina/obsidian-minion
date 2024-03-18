package app.minion.core.functions

import app.minion.core.functions.TaskFunctions.Companion.complete
import app.minion.core.functions.TaskFunctions.Companion.maybeAddDataviewValues
import app.minion.core.model.DataviewField
import app.minion.core.model.DataviewValue
import app.minion.core.model.DateTime
import app.minion.core.model.MinionSettings
import app.minion.core.model.PageTaskField
import app.minion.core.model.PageTaskFieldType
import app.minion.core.model.RepeatInfo
import app.minion.core.model.RepeatSpan
import app.minion.core.model.Tag
import app.minion.util.test.TaskFactory
import arrow.core.toOption
import io.kotest.assertions.arrow.core.shouldBeNone
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.arrow.core.shouldBeSome
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
import kotlinx.datetime.LocalDate

class TaskFunctionsTest : StringSpec({
    "Task.complete() updates a basic task" {
        val task = TaskFactory.createBasicTask()

        val actualEither = task.complete()
        val actual = actualEither.shouldBeRight()

        actual.second.shouldBeNone()
        actual.first.content shouldBeEqual task.content
        actual.first.completedOn.shouldBeSome() // We don't care what the actual value is
        withClue("Completed should be true") {
            actual.first.completed.shouldBeTrue()
        }
    }

    "Task.complete() creates a second task when repeating" {
        val expectedRepeatInfo = RepeatInfo(1, RepeatSpan.DAILY, false)
        val task = TaskFactory.createBasicTask().copy(
            dueDate = DateTime(LocalDate(2050, 1, 2)).toOption(),
            repeatInfo = expectedRepeatInfo.toOption()
        )

        val actualEither = task.complete()
        val actual = actualEither.shouldBeRight()

        val repeatTask = actual.second.shouldBeSome()
        repeatTask.content shouldBeEqual task.content
        repeatTask.dueDate shouldBeSome DateTime(LocalDate(2050, 1, 3))
        repeatTask.repeatInfo shouldBeSome expectedRepeatInfo
    }
    "maybeAddDataviewTags adds tags if setting exists" {
        val settings = MinionSettings.default().copy(pageTaskFields = listOf(
            PageTaskField(DataviewField("Test"), PageTaskFieldType.TAG)
        ))
        val dataview = mapOf(
            DataviewField("Test") to DataviewValue("#foo")
        )
        val tasks = listOf(TaskFactory.createBasicTask())

        val actualEither = tasks.maybeAddDataviewValues(settings, dataview)
        val actual = actualEither.shouldBeRight()

        actual shouldHaveSize 1
        actual[0].tags shouldContainOnly setOf(Tag("foo"), Tag("task"))
    }
})