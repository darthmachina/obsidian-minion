package app.minion.shell.view.codeblock

import app.minion.core.functions.DateTimeFunctions.Companion.toDateTime
import app.minion.core.functions.TaskFilterFunctions.Companion.filterByUpcoming
import app.minion.core.model.DateTime
import app.minion.shell.view.codeblock.CodeBlockTaskFunctions.Companion.maybeAddProperties
import app.minion.util.test.TaskFactory
import arrow.core.some
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainOnly
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

class CodeBlockTaskFunctionsTest : StringSpec({
    "maybeAddProperties adds default properties if properties unset" {
        val config = CodeBlockConfig(query = CodeBlockQuery.tasks, display = CodeBlockDisplay.list)

        val actual = config.maybeAddProperties()
        actual.properties shouldContainOnly listOf(PROPERTY_DUE, PROPERTY_SOURCE, PROPERTY_TAGS, PROPERTY_EISENHOWER)
    }

    "maybeAddProperties does nothing if properties already set" {
        val config = CodeBlockConfig(
            query = CodeBlockQuery.tasks,
            display = CodeBlockDisplay.list,
            properties = listOf(PROPERTY_DUE))

        val actual = config.maybeAddProperties()
        actual.properties shouldContainOnly listOf(PROPERTY_DUE)
    }

    "applyDue filters for upcoming tasks" {
        val config = CodeBlockConfig(
            query = CodeBlockQuery.tasks,
            display = CodeBlockDisplay.list,
            due = listOf(DueOptions.upcoming)
        )

        val now = Clock.System.now()
        val task1 = TaskFactory.createBasicTask().copy(
            dueDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).toDateTime().some()
        )
        val task2 = TaskFactory.createBasicTask().copy(
            dueDate = now.plus(2, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
                .toLocalDateTime(TimeZone.currentSystemDefault()).toDateTime().some()
        )
        val task3 = TaskFactory.createBasicTask().copy(
            dueDate = now.plus(10, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
                .toLocalDateTime(TimeZone.currentSystemDefault()).toDateTime().some()
        )
        val tasks = listOf(task1, task2, task3)

        val actual = tasks.filterByUpcoming()
        actual shouldContainOnly listOf(task2)
    }
})
