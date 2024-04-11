package app.minion.core.formulas

interface FormulaEvaluator { companion object {
    fun FormulaExpression.eval() : FormulaResult {
        return FormulaResult.DecimalResult(1)
    }

    fun FormulaExpression.NumericFormulaExpression.eval() : FormulaResult.DecimalResult = when(this) {
        is FormulaExpression.UnaryNumericFormulaExpression -> this.eval()
        is FormulaExpression.BinaryNumericFormulaExpression -> this.eval()
        is FormulaExpression.ValueNumericFormulaExpression -> this.eval()
        is FormulaExpression.NumericField -> TODO()
    }

    fun FormulaExpression.UnaryNumericFormulaExpression.eval() : FormulaResult.DecimalResult {
        return when (this) {
            is FormulaExpression.Neg -> FormulaResult.DecimalResult(-expr.eval().value)
        }
    }

    fun FormulaExpression.ValueNumericFormulaExpression.eval() : FormulaResult.DecimalResult = when(this) {
        is FormulaExpression.Num -> FormulaResult.DecimalResult(value)
    }
}}
