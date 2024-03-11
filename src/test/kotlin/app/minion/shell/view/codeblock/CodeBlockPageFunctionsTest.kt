package app.minion.shell.view.codeblock

import app.minion.core.model.Filename
import app.minion.core.model.Tag
import app.minion.shell.view.codeblock.CodeBlockPageFunctions.Companion.findFilesMatchingTags
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
            Tag("bax") to setOf(Filename("file2"))
        )

        val actualEither = tagCache.findFilesMatchingTags(setOf(Tag("bar"), Tag("baz")))

        val actual = actualEither.shouldBeRight()
        actual shouldContainOnly setOf(Filename("file1"), Filename("file2"))
    }

    "applyIncludeTags filters to files matching tags" {

    }
})