package app.minion.core.store

import app.minion.core.model.File
import app.minion.core.model.FileData
import app.minion.core.model.Filename
import app.minion.core.model.Tag
import app.minion.core.store.StateFunctions.Companion.replaceData
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.maps.shouldContainExactly

class StateFunctionsTest : StringSpec({
    "replaceData correctly replaces fileData instance" {
        val fileDataMap = mapOf(
            Filename("test") to FileData(Filename("test"), File("test/test.md"), tags = listOf(Tag("old"))),
            Filename("keep") to FileData(Filename("keep"), File("test/keep.md"), tags = listOf(Tag("keep")))
        )
        val updated = FileData(Filename("test"), File("test/test.md"), tags = listOf(Tag("new")))

        val actual = fileDataMap.replaceData(updated)
        actual shouldContainExactly mapOf(
            Filename("keep") to FileData(Filename("keep"), File("test/keep.md"), tags = listOf(Tag("keep"))),
            Filename("test") to updated
        )
    }
})
