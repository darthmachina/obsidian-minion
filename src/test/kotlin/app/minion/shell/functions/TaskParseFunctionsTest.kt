package app.minion.shell.functions

import app.minion.core.functions.TaskParseFunctions.Companion.extractTagSet
import app.minion.core.model.Tag
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize

class TaskParseFunctionsTest : StringSpec({
    "String.extractTagSet() finds task tag" {
        val taskString = "- [ ] Test task #task [due:: 2050-01-02]"

        val actual = taskString.extractTagSet()
        actual shouldHaveSize 1
        actual.contains(Tag("task")).shouldBeTrue()
    }
})