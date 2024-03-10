package app.minion.shell.functions

import app.minion.core.model.FileData
import app.minion.core.model.Filename
import app.minion.shell.functions.VaultReadFunctions.Companion.addBacklinks
import app.minion.shell.functions.VaultReadFunctions.Companion.addTags
import app.minion.util.test.TestCachedMetadata
import app.minion.util.test.TestMetadataCache
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.types.shouldBeSameInstanceAs

class VaultReadFunctionsTest : StringSpec({
//    "FileData.addTags passes if there are no tags in the file" {
//        val cachedMetadata = TestCachedMetadata()
//        val metadataCache = TestMetadataCache(cachedMetadata)
//        val fileData = FileData(Filename("test"))
//
//        val actualEither = fileData.addTags(metadataCache)
//        val actual = actualEither.shouldBeRight()
//        actual shouldBeSameInstanceAs fileData
//    }

//    "FileData.addBacklinks passes if there are no backlinks in the file" {
//        val cachedMetadata = TestCachedMetadata()
//        val metadataCache = TestMetadataCache(cachedMetadata)
//        val fileData = FileData(Filename("test"))
//
//        val actualEither = fileData.addBacklinks(metadataCache)
//        val actual = actualEither.shouldBeRight()
//        actual shouldBeSameInstanceAs fileData
//    }
})