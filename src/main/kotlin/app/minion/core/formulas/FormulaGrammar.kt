package app.minion.core.formulas

import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.leftAssociative
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.skip
import com.github.h0tk3y.betterParse.combinators.use
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser

sealed class FormulaResult {
    data class DecimalResult(val value: Long) : FormulaResult() {
        override fun toString(): String {
            return value.toString()
        }
    }
    data class StringResult(val value: String): FormulaResult() {
        override fun toString(): String {
            return value.toString()
        }
    }
    data class BooleanResult(val value: Boolean): FormulaResult() {
        override fun toString(): String {
            return value.toString()
        }
    }
}

sealed class FormulaExpression {
    sealed class NumericFormulaExpression : FormulaExpression()
    sealed class ValueNumericFormulaExpression(value: Long) : NumericFormulaExpression()
    sealed class UnaryNumericFormulaExpression(value: NumericFormulaExpression) : NumericFormulaExpression()
    sealed class BinaryNumericFormulaExpression(left: NumericFormulaExpression, right: NumericFormulaExpression)
        : NumericFormulaExpression()
    data class Num(val value: Long) : ValueNumericFormulaExpression(value)
    data class Neg(val expr: NumericFormulaExpression) : UnaryNumericFormulaExpression(expr)
    data class Add(val left: NumericFormulaExpression, val right: NumericFormulaExpression)
        : BinaryNumericFormulaExpression(left, right)
    data class Sub(val left: NumericFormulaExpression, val right: NumericFormulaExpression)
        : BinaryNumericFormulaExpression(left, right)
    data class Mul(val left: NumericFormulaExpression, val right: NumericFormulaExpression)
        : BinaryNumericFormulaExpression(left, right)
    data class Div(val left: NumericFormulaExpression, val right: NumericFormulaExpression)
        : BinaryNumericFormulaExpression(left, right)

    // Special case, neither Numeric, String nor Boolean
    data class NumericField(val field: String) : NumericFormulaExpression()
}

class MinionFormulaGrammar : Grammar<FormulaExpression>() {
    @Suppress("unused")
    val ws by regexToken("\\s+", ignore = true)

    val tokenInt by regexToken("\\d+")
    val tokenNum by regexToken("[+-]?(\\d*\\.)?\\d+")
    val tokenField by regexToken("\\w+")
    val tokenLPar by literalToken("(")
    val tokenRPar by literalToken(")")
    val tokenLBrace by literalToken("{")
    val tokenRBrace by literalToken("}")
    val tokenPlus by literalToken("+")
    val tokenMinus by literalToken("-")
    val tokenMult by literalToken("*")
    val tokenDiv by literalToken("/")

    val term: Parser<FormulaExpression> by
        (tokenInt use { FormulaExpression.Num(text.toLong()) }) or
        (skip(tokenMinus) and parser(this::term) map {
            FormulaExpression.Neg(it as FormulaExpression.NumericFormulaExpression)
        }) or
        (skip(tokenLPar) and parser(this::rootParser) and skip(tokenRPar)) or
        (skip(tokenLBrace) and tokenField and skip(tokenRBrace) use { FormulaExpression.NumericField(text)})

    val mulDiv by leftAssociative(term, tokenMult or tokenDiv use { type } ) { l, op, r ->
        val left = l as FormulaExpression.NumericFormulaExpression
        val right = r as FormulaExpression.NumericFormulaExpression
        if (op == tokenMult) {
            FormulaExpression.Mul(left, right)
        } else {
            FormulaExpression.Div(left, right)
        }
    }
    val addSub by leftAssociative(mulDiv, tokenPlus or tokenMinus use { type }) { l, op, r ->
        val left = l as FormulaExpression.NumericFormulaExpression
        val right = r as FormulaExpression.NumericFormulaExpression
        if (op == tokenPlus) {
            FormulaExpression.Add(left, right)
        } else {
            FormulaExpression.Sub(left, right)
        }
    }

    override val rootParser: Parser<FormulaExpression> by addSub
}
