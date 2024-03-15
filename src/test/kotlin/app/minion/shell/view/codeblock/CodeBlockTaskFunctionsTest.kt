package app.minion.shell.view.codeblock

import app.minion.shell.view.codeblock.CodeBlockTaskFunctions.Companion.maybeAddProperties
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainOnly

class CodeBlockTaskFunctionsTest : StringSpec({
    "maybeAddProperties adds default properties if properties unset" {
        val config = CodeBlockConfig(query = CodeBlockQuery.tasks, display = CodeBlockDisplay.list)

        val actual = config.maybeAddProperties()
        actual.properties shouldContainOnly listOf(PROPERTY_DUE, PROPERTY_SOURCE, PROPERTY_TAGS)
    }

    "maybeAddProperties does nothing if properties already set" {
        val config = CodeBlockConfig(
            query = CodeBlockQuery.tasks,
            display = CodeBlockDisplay.list,
            properties = listOf(PROPERTY_DUE))

        val actual = config.maybeAddProperties()
        actual.properties shouldContainOnly listOf(PROPERTY_DUE)
    }
})
