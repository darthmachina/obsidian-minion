package app.minion.shell.view.codeblock

import app.minion.core.functions.DateTimeFunctions.Companion.toDateTime
import app.minion.core.functions.TodoistTaskFunctions.Companion.filterByUpcoming
import app.minion.util.test.TaskFactory
import arrow.core.some
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainOnly
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

class CodeBlockTodoistTaskFunctionsTest : StringSpec({
    "applyDue filters for upcoming tasks" {
        val now = Clock.System.now()
        val task1 = TaskFactory.createBasicTodoistTask().copy(
            due = now.toLocalDateTime(TimeZone.currentSystemDefault()).toDateTime().some()
        )
        val task2 = TaskFactory.createBasicTodoistTask().copy(
            due = now.plus(2, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
                .toLocalDateTime(TimeZone.currentSystemDefault()).toDateTime().some()
        )
        val task3 = TaskFactory.createBasicTodoistTask().copy(
            due = now.plus(10, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
                .toLocalDateTime(TimeZone.currentSystemDefault()).toDateTime().some()
        )
        val tasks = listOf(task1, task2, task3)

        val actual = tasks.filterByUpcoming()
        actual shouldContainOnly listOf(task2)
    }
})
