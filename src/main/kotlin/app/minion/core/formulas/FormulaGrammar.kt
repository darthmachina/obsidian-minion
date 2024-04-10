package app.minion.core.formulas

import me.alllex.parsus.parser.Grammar
import me.alllex.parsus.parser.choose
import me.alllex.parsus.parser.map
import me.alllex.parsus.parser.parser
import me.alllex.parsus.parser.reduce
import me.alllex.parsus.parser.skip
import me.alllex.parsus.token.literalToken
import me.alllex.parsus.token.regexToken

sealed class FormulaResult {
    data class DecimalResult(val value: Double) : FormulaResult()
    data class StringResult(val value: String): FormulaResult()
}

sealed class FormulaExpression {
    data class Int(val value: Long) : FormulaExpression()
    data class Num(val value: Double) : FormulaExpression()
    data class Neg(val expr: FormulaExpression) : FormulaExpression()
    data class Add(val left: FormulaExpression, val right: FormulaExpression) : FormulaExpression()
    data class Sub(val left: FormulaExpression, val right: FormulaExpression) : FormulaExpression()
    data class Mul(val left: FormulaExpression, val right: FormulaExpression) : FormulaExpression()
    data class Div(val left: FormulaExpression, val right: FormulaExpression) : FormulaExpression()

    data class Field(val field: String) : FormulaExpression()
}

class MinionFormulaGrammar : Grammar<FormulaResult>() {
    init {
        regexToken("\\s+", ignored = true)
    }

    val tokenLPar by literalToken("(")
    val tokenRPar by literalToken(")")
    val tokenPlus by literalToken("+")
    val tokenMinus by literalToken("-")
    val tokenMultiply by literalToken("*")
    val tokenDivide by literalToken("/")

    val tokenField by regexToken("""\{([a-zA-z]*)\}""")

    val tokenNum by regexToken("-?\\d+\\.\\d+")
    val tokenInt by regexToken("-?\\d+")

    val integer by parser { tokenInt() } map { FormulaExpression.Int(it.text.toLong()) }
    val number by parser { tokenNum() } map { FormulaExpression.Num(it.text.toDouble()) }

    val add by parser {
        val first = choose(integer, number)
        tokenPlus
        val second = choose(integer, number)
        FormulaExpression.Add(first, second)
    }

    override val root by parser {

        val num1 = tokenInt().text.toInt()
        tokenPlus()
        val num2 = tokenInt().text.toInt()

        FormulaResult.DecimalResult(num1 + num2 * 1.0)
    }
}