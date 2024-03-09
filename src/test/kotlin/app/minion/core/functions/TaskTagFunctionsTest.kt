package app.minion.core.functions

import app.minion.core.MinionError
import app.minion.core.functions.TaskTagFunctions.Companion.findTagWithPrefix
import app.minion.core.model.Tag
import app.minion.util.test.TaskFactory
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.types.shouldBeTypeOf

class TaskTagFunctionsTest : StringSpec({
    "Task.findTagWithPrefix finds tag with prefix" {
        val task = TaskFactory.createTaskWithTag(Tag("prefix/child"))

        val actualEither = task.findTagWithPrefix("prefix")
        actualEither shouldBeRight Tag("prefix/child")
    }

    "Task.findTagWithPrefix returns error if tag not found" {
        val task = TaskFactory.createTaskWithTag(Tag("prefix/child"))

        val actualEither = task.findTagWithPrefix("missing")
        val actual = actualEither.shouldBeLeft()
        actual.shouldBeTypeOf<MinionError.TagPrefixNotFoundError>()
    }
})