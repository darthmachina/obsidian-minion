package app.minion.core.formulas

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equals.shouldBeEqual

class FormulaGrammarTest : StringSpec({
    "Simple addition" {
        val parser = MinionFormulaGrammar()

        val value = "1 + 2"
        val actual = parser.parseToEnd(value)

        actual shouldBeEqual FormulaExpression.Add(FormulaExpression.Num(1), FormulaExpression.Num(2))
    }

    "Simple subtraction" {
        val parser = MinionFormulaGrammar()

        val value = "4 - 3"
        val actual = parser.parseToEnd(value)

        actual shouldBeEqual FormulaExpression.Sub(FormulaExpression.Num(4), FormulaExpression.Num(3))
    }

    "Addition with three operands" {
        val parser = MinionFormulaGrammar()

        val value = "1 + 2 + 3"
        val actual = parser.parseToEnd(value)

        actual shouldBeEqual
                FormulaExpression.Add(
                    FormulaExpression.Add(
                        FormulaExpression.Num(1),
                        FormulaExpression.Num(2)
                    ),
                    FormulaExpression.Num(3)
                )
    }

    "Simple multiplication" {
        val parser = MinionFormulaGrammar()

        val value = "1 * 2"
        val actual = parser.parseToEnd(value)

        actual shouldBeEqual FormulaExpression.Mul(FormulaExpression.Num(1), FormulaExpression.Num(2))
    }

    "Simple division" {
        val parser = MinionFormulaGrammar()

        val value = "4 / 2"
        val actual = parser.parseToEnd(value)

        actual shouldBeEqual FormulaExpression.Div(FormulaExpression.Num(4), FormulaExpression.Num(2))
    }

    "Mix of addition and multiplication" {
        val parser = MinionFormulaGrammar()

        val value = "2 * 3 + 4"
        val actual = parser.parseToEnd(value)

        actual shouldBeEqual
                FormulaExpression.Add(
                    FormulaExpression.Mul(
                        FormulaExpression.Num(2),
                        FormulaExpression.Num(3)
                    ),
                    FormulaExpression.Num(4)
                )
    }
})
