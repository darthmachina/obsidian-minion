package app.minion.core.functions

import app.minion.core.functions.ImageFunctions.Companion.getImageName
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equals.shouldBeEqual

class ImageFunctionsTest : StringSpec({
    "getImageName works for basic embed" {
        val embed = "![[embed.jpg]]"

        val actualEither = embed.getImageName()
        val actual = actualEither.shouldBeRight()

        actual shouldBeEqual "embed.jpg"
    }

    "getImageName works for embed with resize" {
        val embed = "![[embed.jpg|100]]"

        val actualEither = embed.getImageName()
        val actual = actualEither.shouldBeRight()

        actual shouldBeEqual "embed.jpg"
    }
})