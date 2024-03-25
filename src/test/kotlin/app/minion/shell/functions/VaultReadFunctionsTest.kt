package app.minion.shell.functions

import app.minion.core.model.Filename
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.maps.shouldContainExactly

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

    "StateAccumulator.addBacklinks creates the correct backlinkCache" {
        val outlinks = listOf(Filename("out"))

        val acc = StateAccumulator()

        val actualEither = acc.addBacklinks(outlinks, Filename("file"))
        val actual = actualEither.shouldBeRight()

        actual.backlinkCache shouldContainExactly mapOf(
            Filename("out") to setOf(Filename("file"))
        )
    }
})
