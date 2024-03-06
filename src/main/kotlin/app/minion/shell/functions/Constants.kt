package app.minion.shell.functions

val dataviewRegex = """^(\w *):: ([.]*)$""".toRegex()
val inlineDataviewRegex = """\[(\w*):: ([\w! -:]*)\]""".toRegex()
