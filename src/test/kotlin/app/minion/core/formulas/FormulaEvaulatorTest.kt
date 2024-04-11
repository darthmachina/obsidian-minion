package app.minion.core.formulas

import app.minion.core.formulas.FormulaEvaluator.Companion.eval
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equals.shouldBeEqual

class FormulaEvaluatorTest : StringSpec({
    "Evaluates a simple addition formula" {
        val formula = FormulaExpression.Add(FormulaExpression.Num(1), FormulaExpression.Num(2))

        val result = formula.eval()
        result shouldBeEqual FormulaResult.DecimalResult(3)
    }

    "Evaluates a simple subtraction formula" {
        val formula = FormulaExpression.Sub(FormulaExpression.Num(3), FormulaExpression.Num(2))

        val result = formula.eval()
        result shouldBeEqual FormulaResult.DecimalResult(1)
    }

    "Evaluates a simple multiplication formula" {
        val formula = FormulaExpression.Mul(FormulaExpression.Num(3), FormulaExpression.Num(2))

        val result = formula.eval()
        result shouldBeEqual FormulaResult.DecimalResult(6)
    }

    "Evaluates a simple division formula" {
        val formula = FormulaExpression.Div(FormulaExpression.Num(6), FormulaExpression.Num(2))

        val result = formula.eval()
        result shouldBeEqual FormulaResult.DecimalResult(3)
    }

    "Evaluates a nested addition formula" {
        val formula = FormulaExpression.Add(
            FormulaExpression.Add(
                FormulaExpression.Num(6),
                FormulaExpression.Num(2)
            ),
            FormulaExpression.Num(2)
        )

        val result = formula.eval()
        result shouldBeEqual FormulaResult.DecimalResult(10)
    }
})
