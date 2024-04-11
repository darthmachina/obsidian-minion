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
    data class DecimalResult(val value: Long) : FormulaResult()
    data class StringResult(val value: String): FormulaResult()
    data class BooleanResult(val value: Boolean): FormulaResult()
}

sealed class FormulaExpression {
    sealed class NumericFormulaExpression : FormulaExpression()
    data class Num(val value: Long) : NumericFormulaExpression()
    data class Neg(val expr: FormulaExpression) : NumericFormulaExpression()
    data class Add(val left: FormulaExpression, val right: FormulaExpression) : NumericFormulaExpression()
    data class Sub(val left: FormulaExpression, val right: FormulaExpression) : NumericFormulaExpression()
    data class Mul(val left: FormulaExpression, val right: FormulaExpression) : NumericFormulaExpression()
    data class Div(val left: FormulaExpression, val right: FormulaExpression) : NumericFormulaExpression()


    data class Field(val field: String) : FormulaExpression()
}

class MinionFormulaCalcGrammar : Grammar<Long>() {
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

    val term: Parser<Long> by
        (tokenInt use { text.toLong() }) or
        (skip(tokenMinus) and parser(this::term) map { -it }) or
        (skip(tokenLPar) and parser(this::rootParser) and skip(tokenRPar))

    val mulDiv by leftAssociative(term, tokenMult or tokenDiv use { type } ) { l, op, r ->
        if (op == tokenMult) {
            l * r
        } else {
            l / r
        }
    }
    val addSub by leftAssociative(mulDiv, tokenPlus or tokenMinus use { type }) { l, op, r ->
        if (op == tokenPlus) {
            l + r
        } else {
            l - r
        }
    }

    override val rootParser: Parser<Long> by addSub
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
        (skip(tokenMinus) and parser(this::term) map { FormulaExpression.Neg(it)}) or
        (skip(tokenLPar) and parser(this::rootParser) and skip(tokenRPar)) or
        (skip(tokenLBrace) and tokenField and skip(tokenRBrace) use { FormulaExpression.Field(text)})

    val mulDiv by leftAssociative(term, tokenMult or tokenDiv use { type } ) { l, op, r ->
        if (op == tokenMult) {
            FormulaExpression.Mul(l, r)
        } else {
            FormulaExpression.Div(l, r)
        }
    }
    val addSub by leftAssociative(mulDiv, tokenPlus or tokenMinus use { type }) { l, op, r ->
        if (op == tokenPlus) {
            FormulaExpression.Add(l, r)
        } else {
            FormulaExpression.Sub(l, r)
        }
    }

    override val rootParser: Parser<FormulaExpression> by addSub
}
