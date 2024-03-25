package app.minion.shell.view.codeblock

import app.minion.core.model.File
import app.minion.core.model.FileData
import app.minion.core.model.Filename
import app.minion.core.model.Tag
import app.minion.shell.view.codeblock.CodeBlockPageFunctions.Companion.applyIncludeTags
import app.minion.shell.view.codeblock.CodeBlockPageFunctions.Companion.findFilesMatchingTags
import app.minion.shell.view.codeblock.CodeBlockPageFunctions.Companion.getFileData
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainOnly

class CodeBlockPageFunctionsTest : StringSpec({
    "findFilesMatchingTags pulls list for a single tag" {
        val tagCache = mapOf(
            Tag("foo") to setOf(Filename("file1"), Filename("file2")),
            Tag("bar") to setOf(Filename("file1"))
        )

        val actualEither = tagCache.findFilesMatchingTags(setOf(Tag("bar")))

        val actual = actualEither.shouldBeRight()
        actual shouldContainOnly setOf(Filename("file1"))
    }

    "findFilesMatchingTags pulls list for a multiple tags" {
        val tagCache = mapOf(
            Tag("foo") to setOf(Filename("file1"), Filename("file2")),
            Tag("bar") to setOf(Filename("file1")),
            Tag("baz") to setOf(Filename("file2"))
        )

        val actualEither = tagCache.findFilesMatchingTags(setOf(Tag("bar"), Tag("baz")))

        val actual = actualEither.shouldBeRight()
        actual shouldContainOnly setOf(Filename("file1"), Filename("file2"))
    }

    "applyIncludeTags filters to files matching tags" {
        val filenames = setOf(Filename("file1"), Filename("file3"))
        val tagCache = mapOf(
            Tag("foo") to setOf(Filename("file1"), Filename("file2")),
            Tag("bar") to setOf(Filename("file1")),
            Tag("baz") to setOf(Filename("file2"))
        )
        val config = CodeBlockConfig(
            display = CodeBlockDisplay.list,
            query = CodeBlockQuery.pages,
            include = IncludeExcludeOptions(
                tags = listOf("bar")
            )
        )

        val actualEither = filenames.applyIncludeTags(tagCache, config)

        val actual = actualEither.shouldBeRight()
        actual shouldContainOnly setOf(Filename("file1"))
    }

    "getFileData returns correct instances" {
        val fileData = mapOf(
            Filename("foo") to FileData(Filename("foo"), File("/foo.md")),
            Filename("bar") to FileData(Filename("bar"), File("/foo.md")),
        )
        val files = setOf(Filename("foo"))

        val actualEither = files.getFileData(fileData)
        val actual = actualEither.shouldBeRight()

        actual shouldContainOnly listOf(fileData[Filename("foo")]!!)
    }
})
