package app.minion.core.functions

import app.minion.core.functions.PageFilterFunctions.Companion.excludeByDataview
import app.minion.core.functions.PageFilterFunctions.Companion.filterBySource
import app.minion.core.model.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainOnly

class PageFilterFunctionsTest : StringSpec({
    "filterBySource filters for a string" {
        val file1 = FileData(Filename("File1"), File("File1.md"))
        val file2 = FileData(Filename("File2"), File("File1.md"))

        val source = listOf("File1")
        val files = listOf(file1, file2)

        val actual = files.filterBySource(source)
        actual shouldContainOnly listOf(file1)
    }

    "filterBySource filters for a regex" {
        val file1 = FileData(Filename("Include1"), File("Include1.md"))
        val file2 = FileData(Filename("Skip1"), File("Skip1.md"))
        val file3 = FileData(Filename("Include2"), File("Include2.md"))
        val file4 = FileData(Filename("Skip2"), File("Skip2.md"))

        val source = listOf("/Include[0-9]+/")
        val files = listOf(file1, file2, file3, file4)

        val actual = files.filterBySource(source)
        actual shouldContainOnly listOf(file1, file3)
    }

    "excludeByDataview correctly excludes" {
        val file1 = FileData(
            Filename("Include1"),
            File("Include1.md"),
            dataview = mapOf(
                DataviewField("Field") to DataviewValue("Exclude")
            )
        )
        val file2 = FileData(
            Filename("Skip1"),
            File("Skip1.md"),
            dataview = mapOf(
                DataviewField("Field") to DataviewValue("Include")
            )
        )
        val file3 = FileData(
            Filename("Include2"),
            File("Include2.md"),
            dataview = mapOf(
                DataviewField("Field") to DataviewValue("Exclude")
            )
        )
        val file4 = FileData(
            Filename("Skip2"),
            File("Skip2.md"),
            dataview = mapOf(
                DataviewField("Field") to DataviewValue("Include")
            )
        )

        val files = listOf(file1, file2, file3, file4)
        val actual = files.excludeByDataview(
            listOf(DataviewField("Field") to DataviewValue("Exclude"))
        )
        actual shouldContainOnly listOf(file2, file4)
    }
})