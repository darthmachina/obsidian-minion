package app.minion.core.formulas

import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.lexer.DefaultTokenizer
import com.github.h0tk3y.betterParse.lexer.regexToken
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equals.shouldBeEqual

class FormulaGrammarTest : StringSpec({
    "Basic addition grammar parses" {
        val parser = MinionFormulaGrammar()

        val addition = "1 + 2"
        val actual = parser.parseToEnd(addition)

        actual shouldBeEqual "1 + 2"
    }

    "Testing" {
        val tokenInt = regexToken("\\d+")

        val matches = DefaultTokenizer(listOf(tokenInt)).tokenize("1")
        println("mathces: $matches")
        val result = tokenInt.tryParse(matches, 0)
        println("result: $result")
    }
})
