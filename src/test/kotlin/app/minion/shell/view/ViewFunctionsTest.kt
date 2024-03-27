package app.minion.shell.view

import app.minion.core.model.Content
import app.minion.shell.view.ViewFunctions.Companion.tokenize
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly

class ViewFunctionsTesta: StringSpec({
    "tokenize parses basic bold" {
        val content = Content("This is **bold**")

        val actual = content.tokenize()

        actual shouldContainExactly listOf(
            "This is ",
            "!bbold",
            ""
        )
    }

    "tokenize parses bold with hash" {
        val content = Content("This is **#bold**")

        val actual = content.tokenize()

        actual shouldContainExactly listOf(
            "This is ",
            "!b#bold",
            ""
        )
    }
    "tokenize parses basic italic" {
        val content = Content("This is *italic*")

        val actual = content.tokenize()

        actual shouldContainExactly listOf(
            "This is ",
            "!iitalic",
            ""
        )
    }

    "tokenize parses italic with hash" {
        val content = Content("This is *#italic*")

        val actual = content.tokenize()

        actual shouldContainExactly listOf(
            "This is ",
            "!i#italic",
            ""
        )
    }

    "tokenize parses basic code" {
        val content = Content("This is `code`")

        val actual = content.tokenize()

        actual shouldContainExactly listOf(
            "This is ",
            "!ccode",
            ""
        )
    }

    "tokenize parses code with hash" {
        val content = Content("This is `#code`")

        val actual = content.tokenize()

        actual shouldContainExactly listOf(
            "This is ",
            "!c#code",
            ""
        )
    }
})
