package app.minion.shell.view.codeblock

import app.minion.core.model.Tag
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
import net.mamoe.yamlkt.Yaml

class CodeBlockConfigTest : StringSpec({
    "Decodes config with just query and display set" {
        val yaml = """
            query: tasks
            display: table
        """.trimIndent()
        val actual = Yaml.decodeFromString(CodeBlockConfig.serializer(), yaml)

        actual.display shouldBeEqual CodeBlockDisplay.table
    }

    "Decodes config with due date" {
        val yaml = """
            query: tasks
            display: table
            due:
                - today
                - overdue
        """.trimIndent()

        val actual = Yaml.decodeFromString(CodeBlockConfig.serializer(), yaml)

        actual.display shouldBeEqual CodeBlockDisplay.table
        actual.due shouldHaveSize 2
        actual.due shouldContainOnly listOf(DueOptions.today, DueOptions.overdue)
    }

    "Decodes config with include options" {
        val yaml = """
            query: tasks
            display: table
            include:
                tags:
                    - foo
        """.trimIndent()

        val actual = Yaml.decodeFromString(CodeBlockConfig.serializer(), yaml)

        actual.display shouldBeEqual CodeBlockDisplay.table
        actual.include.tags shouldHaveSize 1
        actual.include.tags shouldContainOnly listOf(Tag("foo"))
    }
})
