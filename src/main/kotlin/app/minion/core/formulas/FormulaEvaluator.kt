package app.minion.core.formulas

interface FormulaEvaluator { companion object {
    fun FormulaExpression.eval() : FormulaResult = when(this) {
        is FormulaExpression.NumericFormulaExpression -> this.eval()
    }

    fun FormulaExpression.NumericFormulaExpression.eval() : FormulaResult.DecimalResult = when(this) {
        is FormulaExpression.UnaryNumericFormulaExpression -> this.eval()
        is FormulaExpression.BinaryNumericFormulaExpression -> this.eval()
        is FormulaExpression.ValueNumericFormulaExpression -> this.eval()
        is FormulaExpression.NumericField -> TODO()
    }

    fun FormulaExpression.UnaryNumericFormulaExpression.eval() : FormulaResult.DecimalResult = when (this) {
        is FormulaExpression.Neg -> FormulaResult.DecimalResult(-expr.eval().value)
    }

    fun FormulaExpression.BinaryNumericFormulaExpression.eval() : FormulaResult.DecimalResult = when(this) {
        is FormulaExpression.Add -> FormulaResult.DecimalResult(left.eval().value + right.eval().value)
        is FormulaExpression.Sub -> FormulaResult.DecimalResult(left.eval().value - right.eval().value)
        is FormulaExpression.Mul -> FormulaResult.DecimalResult(left.eval().value * right.eval().value)
        is FormulaExpression.Div -> FormulaResult.DecimalResult(left.eval().value / right.eval().value)
    }

    fun FormulaExpression.ValueNumericFormulaExpression.eval() : FormulaResult.DecimalResult = when(this) {
        is FormulaExpression.Num -> FormulaResult.DecimalResult(value)
    }
}}
