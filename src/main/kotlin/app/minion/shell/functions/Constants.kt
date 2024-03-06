package app.minion.shell.functions

val dataviewRegex = """^([\w ]*):: (.*)$""".toRegex(RegexOption.MULTILINE)
val inlineDataviewRegex = """\[(\w*):: ([\w! -:]*)\]""".toRegex()
