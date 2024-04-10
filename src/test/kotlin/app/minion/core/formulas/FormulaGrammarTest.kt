package app.minion.core.formulas

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equals.shouldBeEqual

class FormulaGrammarTest : StringSpec({
    "Basic grammar parses" {
        val parser = MinionFormulaGrammar()

        val result = parser.parseOrThrow("1 + 2") as FormulaResult.DecimalResult
        result.value shouldBeEqual 3.0
    }
})