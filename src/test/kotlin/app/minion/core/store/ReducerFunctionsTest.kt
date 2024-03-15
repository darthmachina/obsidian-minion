package app.minion.core.store

import app.minion.core.model.File
import app.minion.core.model.FileData
import app.minion.core.model.Filename
import app.minion.core.model.MinionSettings
import app.minion.core.store.ReducerFunctions.Companion.filenamesForExcludedFolders
import app.minion.core.store.ReducerFunctions.Companion.filterExcludedFolders
import app.minion.util.test.TaskFactory
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainOnly

class ReducerFunctionsTest : StringSpec({
    "List<Task>.filterExcludedFolders filters tasks" {
        val settings = MinionSettings("2", emptyMap(), setOf("Exclude"))

        val templateTask = TaskFactory.createBasicTask()
        val keepTask = templateTask.copy(
            fileInfo = templateTask.fileInfo.copy(path = File("Keep/KeepTask.md"))
        )
        val excludeTask = templateTask.copy(
            fileInfo = templateTask.fileInfo.copy(path = File("Exclude/ExcludeTask.md"))
        )

        val tasks = listOf(keepTask, excludeTask)

        val actualEither = tasks.filterExcludedFolders(settings)
        val actual = actualEither.shouldBeRight()

        actual shouldContainOnly listOf(keepTask)
    }

    "List<Task>.filterExcludedFolders works when setting is empty" {
        val settings = MinionSettings("2", emptyMap(), emptySet())

        val templateTask = TaskFactory.createBasicTask()
        val keepTask = templateTask.copy(
            fileInfo = templateTask.fileInfo.copy(path = File("Keep/KeepTask.md"))
        )
        val excludeTask = templateTask.copy(
            fileInfo = templateTask.fileInfo.copy(path = File("Exclude/ExcludeTask.md"))
        )

        val tasks = listOf(keepTask, excludeTask)

        val actualEither = tasks.filterExcludedFolders(settings)
        val actual = actualEither.shouldBeRight()

        actual shouldContainOnly listOf(keepTask, excludeTask)
    }

    "filenamesForExcludeFolders pulls out correct filenames" {
        val settings = MinionSettings("2", emptyMap(), setOf("Exclude"))

        val fileCache = mapOf(
            Filename("Keep") to FileData(Filename("Keep"), File("Keep/KeepTask.md")),
            Filename("Exclude") to FileData(Filename("Exclude"), File("Exclude/ExcludeTask.md"))
        )

        val actualEither = fileCache.filenamesForExcludedFolders(settings)
        val actual = actualEither.shouldBeRight()
        actual shouldContainOnly listOf(Filename("Exclude"))
    }

    "filenamesForExcludedFolders works when setting is empty" {
        val settings = MinionSettings("2", emptyMap(), emptySet())

        val fileCache = mapOf(
            Filename("Keep") to FileData(Filename("Keep"), File("Keep/KeepTask.md")),
            Filename("Exclude") to FileData(Filename("Exclude"), File("Exclude/ExcludeTask.md"))
        )

        val actualEither = fileCache.filenamesForExcludedFolders(settings)
        val actual = actualEither.shouldBeRight()
        actual.shouldBeEmpty()
    }
})