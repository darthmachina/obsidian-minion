package app.minion.core.formulas

import app.minion.core.MinionError
import arrow.core.Either
import arrow.core.flatMap
import arrow.core.raise.either
import arrow.core.toOption

interface FormulaEvaluator { companion object {
    fun FormulaExpression.eval(fields: Map<String, String>) : Either<MinionError, FormulaResult> = either {
        when(this@eval) {
            is FormulaExpression.NumericFormulaExpression -> this@eval.eval(fields).bind()
        }
    }

    fun FormulaExpression.NumericFormulaExpression.eval(fields: Map<String, String>)
    : Either<MinionError, FormulaResult.DecimalResult> = either {
        when (this@eval) {
            is FormulaExpression.UnaryNumericFormulaExpression -> this@eval.eval(fields).bind()
            is FormulaExpression.BinaryNumericFormulaExpression -> this@eval.eval(fields).bind()
            is FormulaExpression.ValueNumericFormulaExpression -> this@eval.eval(fields).bind()
            is FormulaExpression.NumericField -> this@eval.eval(fields).bind()
        }
    }

    fun FormulaExpression.UnaryNumericFormulaExpression.eval(fields: Map<String, String>)
    : Either<MinionError, FormulaResult.DecimalResult> = either {
        when (this@eval) {
            is FormulaExpression.Neg -> FormulaResult.DecimalResult(-expr.eval(fields).bind().value)
        }
    }

    fun FormulaExpression.BinaryNumericFormulaExpression.eval(fields: Map<String, String>)
    : Either<MinionError, FormulaResult.DecimalResult> = either {
        when (this@eval) {
            is FormulaExpression.Add ->
                FormulaResult.DecimalResult(left.eval(fields).bind().value + right.eval(fields).bind().value)
            is FormulaExpression.Sub ->
                FormulaResult.DecimalResult(left.eval(fields).bind().value - right.eval(fields).bind().value)
            is FormulaExpression.Mul ->
                FormulaResult.DecimalResult(left.eval(fields).bind().value * right.eval(fields).bind().value)
            is FormulaExpression.Div ->
                FormulaResult.DecimalResult(left.eval(fields).bind().value / right.eval(fields).bind().value)
        }
    }

    fun FormulaExpression.ValueNumericFormulaExpression.eval(fields: Map<String, String>)
    : Either<MinionError, FormulaResult.DecimalResult> = either {
        when (this@eval) {
            is FormulaExpression.Num -> FormulaResult.DecimalResult(value)
        }
    }

    fun FormulaExpression.NumericField.eval(fields: Map<String, String>)
    : Either<MinionError, FormulaResult.DecimalResult> = either {
        fields[this@eval.field]
            .toOption()
            .toEither {
                MinionError.FieldMissingError("${this@eval.field} does not exist in fieldset")
            }
            .flatMap {
                it
                    .toLongOrNull()
                    .toOption()
                    .toEither {
                        MinionError.FieldValueCoercionError("Cannot use '$it' as a number for field ${this@eval.field}")
                    }
                    .map {
                        FormulaResult.DecimalResult(it)
                    }
            }
            .bind()
    }
}}
