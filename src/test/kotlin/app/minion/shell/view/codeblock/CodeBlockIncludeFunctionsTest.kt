package app.minion.shell.view.codeblock

import app.minion.core.model.Tag
import app.minion.core.model.TagType
import app.minion.shell.view.codeblock.CodeBlockTaskIncludeFunctions.Companion.applyInclude
import app.minion.shell.view.codeblock.CodeBlockTaskIncludeFunctions.Companion.applyIncludeTagsAnd
import app.minion.shell.view.codeblock.CodeBlockTaskIncludeFunctions.Companion.applyIncludeTagsOr
import app.minion.util.test.TaskFactory
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly

class CodeBlockIncludeFunctionsTest : StringSpec({
    "applyIncludeTagsAnd work for page tags" {
        val task1 = TaskFactory.createBasicTask().copy(tags = setOf(Tag("regular")))
        val task2 = TaskFactory.createBasicTask().copy(tags = setOf(Tag("page", TagType.PAGE)))
        val tasks = listOf(task1, task2)

        val actual = tasks.applyIncludeTagsAnd(IncludeExcludeOptions(tags = listOf("page")))
        actual shouldContainExactly listOf(task2)
    }

    "applyIncludeTagsOr returns correct list" {
        val task1 = TaskFactory.createBasicTask().copy(tags = setOf(Tag("tag1")))
        val task2 = TaskFactory.createBasicTask().copy(tags = setOf(Tag("tag2")))
        val task3 = TaskFactory.createBasicTask().copy(tags = setOf(Tag("tag3")))
        val tasks = listOf(task1, task2, task3)

        val includeOptions = IncludeExcludeOptions(tags = listOf("tag1", "tag2"))

        val actual = tasks.applyIncludeTagsOr(includeOptions)

        actual shouldContainExactly listOf(task1, task2)
    }

    "applyInclude for or block" {
        val task1 = TaskFactory.createBasicTask().copy(tags = setOf(Tag("tag1")))
        val task2 = TaskFactory.createBasicTask().copy(tags = setOf(Tag("tag2")))
        val task3 = TaskFactory.createBasicTask().copy(tags = setOf(Tag("tag3")))
        val tasks = listOf(task1, task2, task3)

        val includeOptions = IncludeExcludeOptions(
            or = listOf(IncludeExcludeOptions(tags = listOf("tag1", "tag2"))))

        val actual = tasks.applyInclude(includeOptions)

        actual shouldContainExactly listOf(task1, task2)
    }

    "applyInclude for and block" {
        val task1 = TaskFactory.createBasicTask().copy(tags = setOf(Tag("tag1")))
        val task2 = TaskFactory.createBasicTask().copy(tags = setOf(Tag("tag2"), Tag("tag1")))
        val task3 = TaskFactory.createBasicTask().copy(tags = setOf(Tag("tag3")))
        val tasks = listOf(task1, task2, task3)

        val includeOptions = IncludeExcludeOptions(
            and = listOf(IncludeExcludeOptions(tags = listOf("tag1", "tag2")))
        )

        val actual = tasks.applyInclude(includeOptions)

        actual shouldContainExactly listOf(task2)
    }

    "applyInclude for mix of and and or blocks" {
        val task1 = TaskFactory.createBasicTask().copy(tags = setOf(Tag("area1"), Tag("tag1")))
        val task2 = TaskFactory.createBasicTask().copy(tags = setOf(Tag("area1"), Tag("tag2")))
        val task3 = TaskFactory.createBasicTask().copy(tags = setOf(Tag("area2"), Tag("tag2")))
        val task4 = TaskFactory.createBasicTask().copy(tags = setOf(Tag("area1"), Tag("tag3")))
        val tasks = listOf(task1, task2, task3, task4)

        val includeOptions = IncludeExcludeOptions(
            and = listOf(
                IncludeExcludeOptions(
                    or = listOf(IncludeExcludeOptions(tags = listOf("tag1", "tag2")))
                ),
                IncludeExcludeOptions(tags = listOf("area1"))
            )
        )

        val actual = tasks.applyInclude(includeOptions)

        actual shouldContainExactly listOf(task1, task2)
    }
})
