package app.minion.shell.view.codeblock

import app.minion.core.model.Tag
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.collections.shouldHaveSingleElement
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
        actual.include.tags shouldContainOnly listOf("foo")
    }

    "Decodes config with exclude options" {
        val yaml = """
            query: tasks
            display: table
            exclude:
                tags:
                    - foo
        """.trimIndent()

        val actual = Yaml.decodeFromString(CodeBlockConfig.serializer(), yaml)

        actual.display shouldBeEqual CodeBlockDisplay.table
        actual.exclude.tags shouldHaveSize 1
        actual.exclude.tags shouldContainOnly listOf("foo")
    }

    "Decodes config with limit set" {
        val yaml = """
            query: tasks
            display: table
            limit: 10
        """.trimIndent()

        val actual = Yaml.decodeFromString(CodeBlockConfig.serializer(), yaml)

        actual.display shouldBeEqual CodeBlockDisplay.table
        actual.limit shouldBeEqual 10
    }

    "Decodes config with group by set" {
        val yaml = """
            query: tasks
            display: table
            groupBy: source
        """.trimIndent()

        val actual = Yaml.decodeFromString(CodeBlockConfig.serializer(), yaml)

        actual.display shouldBeEqual CodeBlockDisplay.table
        actual.groupBy shouldBeEqual GroupByOptions.source
    }

    "Decodes config with options set" {
        val yaml = """
            query: tasks
            display: table
            options:
              - image_on_cover
        """.trimIndent()

        val actual = Yaml.decodeFromString(CodeBlockConfig.serializer(), yaml)

        actual.display shouldBeEqual CodeBlockDisplay.table
        actual.options shouldHaveSingleElement CodeBlockOptions.image_on_cover
    }
})
