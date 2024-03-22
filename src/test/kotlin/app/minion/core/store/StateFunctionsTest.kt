package app.minion.core.store

import app.minion.core.model.DataviewField
import app.minion.core.model.DataviewValue
import app.minion.core.model.File
import app.minion.core.model.FileData
import app.minion.core.model.Filename
import app.minion.core.model.Tag
import app.minion.core.store.StateFunctions.Companion.updateDataviewCache
import app.minion.core.store.StateFunctions.Companion.updateTagCache
import app.minion.core.store.StateFunctions.Companion.replaceData
import app.minion.core.store.StateFunctions.Companion.updateBacklinkCache
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

    "updateTagCache adds FileData to tag cache" {
        val fileData = FileData(
            Filename("test"),
            File("test.md"),
            tags = listOf(Tag("tag"))
        )
        val tagCache = emptyMap<Tag, Set<Filename>>()

        val actual = fileData.updateTagCache(tagCache)

        actual shouldContainExactly mapOf(
            Tag("tag") to setOf(fileData.name)
        )
    }

    "updateTagCache replaces tags for fileData if tag is removed" {
        val fileData = FileData(
            Filename("test"),
            File("test.md"),
            tags = listOf(Tag("new"))
        )
        val tagCache = mapOf(Tag("old") to setOf(fileData.name))

        val actual = fileData.updateTagCache(tagCache)

        actual shouldContainExactly mapOf(
            Tag("new") to setOf(fileData.name),
            Tag("old") to emptySet()
        )
    }

    "updateDataviewCache adds fileData to cache" {
        val fileData = FileData(
            Filename("test"),
            File("test.md"),
            dataview = mapOf(DataviewField("field") to DataviewValue("value"))
        )
        val dataviewCache = emptyMap<Pair<DataviewField, DataviewValue>, Set<Filename>>()

        val actual = fileData.updateDataviewCache(dataviewCache)

        actual shouldContainExactly mapOf(
            Pair(DataviewField("field"), DataviewValue("value")) to setOf(fileData.name)
        )
    }

    "updateDataviewCache replaces entries for fileData" {
        val fileData = FileData(
            Filename("test"),
            File("test.md"),
            dataview = mapOf(DataviewField("field") to DataviewValue("newvalue"))
        )
        val dataviewCache = mapOf(Pair(DataviewField("field"), DataviewValue("oldvalue")) to setOf(fileData.name))

        val actual = fileData.updateDataviewCache(dataviewCache)

        actual shouldContainExactly mapOf(
            Pair(DataviewField("field"), DataviewValue("newvalue")) to setOf(fileData.name),
            Pair(DataviewField("field"), DataviewValue("oldvalue")) to emptySet()
        )
    }

    "updateBacklinkCache adds fileData to cache" {
        val fileData = FileData(
            Filename("test"),
            File("test.md"),
            outLinks = listOf(Filename("out"))
        )
        val backlinkCache = emptyMap<Filename, Set<Filename>>()

        val actual = fileData.updateBacklinkCache(backlinkCache)

        actual shouldContainExactly mapOf(
            Filename("out") to setOf(fileData.name)
        )
    }

    "updateBacklinkCache replaces entries for fileData" {
        val fileData = FileData(
            Filename("test"),
            File("test.md"),
            outLinks = listOf(Filename("newout"))
        )
        val backlinkCache = mapOf(Filename("oldout") to setOf(fileData.name))

        val actual = fileData.updateBacklinkCache(backlinkCache)

        actual shouldContainExactly mapOf(
            Filename("newout") to setOf(fileData.name),
            Filename("oldout") to emptySet()
        )
    }
})
