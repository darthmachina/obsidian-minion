package app.minion.shell.view.codeblock

import app.minion.core.model.Tag
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.collections.shouldHaveSingleElement
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
import kotlinx.serialization.decodeFromString
import mu.KotlinLogging
import mu.KotlinLoggingConfiguration
import mu.KotlinLoggingLevel
import net.mamoe.yamlkt.Yaml

class CodeBlockConfigTest : StringSpec({
    "Decodes config with just query and display set" {
        val yaml = """
            query: todoist
            display: table
        """.trimIndent()
        val actual = Yaml.decodeFromString(CodeBlockConfig.serializer(), yaml)

        actual.display shouldBeEqual CodeBlockDisplay.table
    }

    "Decodes config with due date" {
        val yaml = """
            query: todoist
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
            query: todoist
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
            query: todoist
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
            query: todoist
            display: table
            limit: 10
        """.trimIndent()

        val actual = Yaml.decodeFromString(CodeBlockConfig.serializer(), yaml)

        actual.display shouldBeEqual CodeBlockDisplay.table
        actual.limit shouldBeEqual 10
    }

    "Decodes config with group by set" {
        val yaml = """
            query: todoist
            display: table
            groupBy: source
        """.trimIndent()

        val actual = Yaml.decodeFromString(CodeBlockConfig.serializer(), yaml)

        actual.display shouldBeEqual CodeBlockDisplay.table
        actual.groupBy shouldBeEqual GroupByOptions.source
    }

    "Decodes config with options set" {
        val yaml = """
            query: todoist
            display: table
            options:
              - image_on_cover
        """.trimIndent()

        val actual = Yaml.decodeFromString(CodeBlockConfig.serializer(), yaml)

        actual.display shouldBeEqual CodeBlockDisplay.table
        actual.options shouldHaveSingleElement CodeBlockOptions.image_on_cover
    }

    "Decodes config with and include with nested or" {
        val yaml = """
            query: todoist
            display: table
            include:
              and:
                - or:
                  - tags:
                    - tag1
                    - tag2
                  - links:
                    - link
        """.trimIndent()
        val actual = Yaml.decodeFromString(CodeBlockConfig.serializer(), yaml)

        actual.include.and shouldContainExactly listOf(
            IncludeExcludeOptions(or = listOf(
                IncludeExcludeOptions(
                    tags = listOf("tag1", "tag2")
                ),
                IncludeExcludeOptions(
                    links = listOf("link")
                )
            ))
        )
    }

    "Decodes config with and include mixed with nested or" {
        val yaml = """
            query: todoist
            display: table
            include:
              and:
                - or:
                  - tags:
                    - tag1
                    - tag2
                - links:
                  - link
        """.trimIndent()
        val actual = Yaml.decodeFromString(CodeBlockConfig.serializer(), yaml)

        actual.include.and shouldContainExactly listOf(
            IncludeExcludeOptions(or = listOf(
                IncludeExcludeOptions(
                    tags = listOf("tag1", "tag2")
                )
            )),
            IncludeExcludeOptions(
                links = listOf("link")
            )
        )
    }

    "Decodes config with complex dataview includes" {
        val yaml = """
            query: pages
            display: gallery
            include:
              and:
                - dataview:
                  - "WithMina:: ✅"
                - or:
                  - dataview:
                    - "Status:: #media/inprogress"
                    - "Status:: #media/next"
            groupBy: dataview
            groupByField: Status
        """.trimIndent()

        val actual = Yaml.decodeFromString(CodeBlockConfig.serializer(), yaml)

        actual.include.and shouldContainExactly listOf(
            IncludeExcludeOptions(dataview = listOf("WithMina:: ✅")),
            IncludeExcludeOptions(or = listOf(
                IncludeExcludeOptions(dataview = listOf("Status:: #media/inprogress", "Status:: #media/next"))
            ))
        )
    }
})
