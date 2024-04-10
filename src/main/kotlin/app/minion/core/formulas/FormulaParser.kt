package app.minion.core.formulas

import cc.ekblad.konbini.Parser
import cc.ekblad.konbini.parser
import cc.ekblad.konbini.char
import cc.ekblad.konbini.decimal
import cc.ekblad.konbini.whitespace

val division = Parser<Double> = parser {
    val lhs = decimal()
    whitespace()
    char('/')
    whitespace()
    val rhs = decimal()
    lhs / rhs
}
