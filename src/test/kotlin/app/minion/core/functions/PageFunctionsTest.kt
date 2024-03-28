package app.minion.core.functions

import app.minion.core.functions.PageFunctions.Companion.upsertDataviewValue
import app.minion.core.model.DataviewField
import app.minion.core.model.DataviewValue
import arrow.core.None
import arrow.core.some
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equals.shouldBeEqual

class PageFunctionsTest : StringSpec({
    "upsertDataviewValue replaces existing value" {
        val page = """
            Keep:: keep
            Change:: oldvalue
        """.trimIndent()

        val actualEither = page.upsertDataviewValue(
            DataviewField("Change"), DataviewValue("oldvalue").some(), DataviewValue("newvalue"))
        val actual = actualEither.shouldBeRight()

        actual shouldBeEqual """
            Keep:: keep
            Change:: newvalue
        """.trimIndent()
    }

    "upsertDataviewValue adds value to empty field" {
        val page = """
            Keep:: keep
            Change:: 
        """.trimIndent()

        val actualEither = page.upsertDataviewValue(DataviewField("Change"), None, DataviewValue("newvalue"))
        val actual = actualEither.shouldBeRight()

        actual shouldBeEqual """
            Keep:: keep
            Change:: newvalue 
        """.trimIndent()
    }

    "upsertDataviewValue adds field/value if non-existent" {
        val page = """
            Keep:: keep
        """.trimIndent()

        val actualEither = page.upsertDataviewValue(DataviewField("Change"), None, DataviewValue("newvalue"))
        val actual = actualEither.shouldBeRight()

        actual shouldBeEqual """
            Change:: newvalue
            Keep:: keep
        """.trimIndent()
    }
})
