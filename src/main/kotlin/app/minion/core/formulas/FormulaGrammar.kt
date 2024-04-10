package app.minion.core.formulas

import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.skip
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser

sealed class FormulaResult {
    data class DecimalResult(val value: Double) : FormulaResult()
    data class StringResult(val value: String): FormulaResult()
    data class BooleanResult(val value: Boolean): FormulaResult()
}

sealed class FormulaExpression {
    data class Num(val value: Long) : FormulaExpression()
    data class Dec(val value: Double) : FormulaExpression()
    data class Neg(val expr: FormulaExpression) : FormulaExpression()
    data class Add(val left: FormulaExpression, val right: FormulaExpression) : FormulaExpression()
    data class Sub(val left: FormulaExpression, val right: FormulaExpression) : FormulaExpression()
    data class Mul(val left: FormulaExpression, val right: FormulaExpression) : FormulaExpression()
    data class Div(val left: FormulaExpression, val right: FormulaExpression) : FormulaExpression()

    data class Field(val field: String) : FormulaExpression()
}

class MinionFormulaGrammar : Grammar<String>() {
    @Suppress("unused")
    val ws by regexToken("\\s+", ignore = true)

    val tokenInt by regexToken("\\d+")
    val tokenNum by regexToken("[+-]?(\\d*\\.)?\\d+")
    val tokenPlus by literalToken("+")

    val addition by tokenInt and skip(tokenPlus) and tokenInt map { (l, r) -> "${l.text} + ${r.text}" }
    val number by tokenInt map {  }

    override val rootParser: Parser<String> by addition
}
