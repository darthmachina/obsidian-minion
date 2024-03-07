package app.minion.shell.view.codeblock

import io.kotest.core.spec.style.StringSpec
import net.mamoe.yamlkt.Yaml

class CodeBlockConfigTest : StringSpec({
    "YAML decodes config with just display set" {
        val displayYaml = """
            display: table
        """.trimIndent()
        val config = Yaml.decodeFromString(CodeBlockConfig.serializer(), displayYaml)
    }
})
