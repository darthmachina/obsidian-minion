package app.minion.core.formulas

interface FormulaEvaluator { companion object {
    fun FormulaExpression.eval() : FormulaResult {
        return FormulaResult.DecimalResult(1)
    }

    fun FormulaExpression.UnaryFormulaExpression.eval() : FormulaResult {
        return when (this) {
            is FormulaExpression.Num -> FormulaResult.DecimalResult(value)
            is FormulaExpression.Neg -> {
                expr
                    .eval()

            }

            is FormulaExpression.NumericField -> TODO()
        }
    }
}}