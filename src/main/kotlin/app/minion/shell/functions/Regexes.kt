package app.minion.shell.functions

val dataviewRegex = """^([\w ]*):: (.*)$""".toRegex(RegexOption.MULTILINE)
val inlineDataviewRegex = """\[(\w*):: ([\w! -_:]*)\]""".toRegex()
val taskSyntaxRegex = "^(\\s)*- \\[[xX ]\\] ".toRegex()
val noteSyntaxRegex = "^(\\s)*- ".toRegex()
val allTagsRegex = """#([a-zA-Z][0-9a-zA-Z-_/]*)""".toRegex()
