package app.minion.core.store

import app.minion.core.MinionError
import app.minion.core.model.*
import arrow.core.Either
import arrow.core.raise.either

interface StateFunctions { companion object {
    fun State.updateState(fileData: FileData) : Either<MinionError, State> = either {
        // Add filenames from State for all tags in fileData
        // Add updated map back tp tagCache
        this@updateState.copy(
            files = this@updateState.files.replaceData(fileData),
            tagCache = fileData.addToTagCache(this@updateState.tagCache),
            dataviewCache = fileData.addToDataviewCache(this@updateState.dataviewCache),
            backlinkCache = fileData.addToBacklinkCache(this@updateState.backlinkCache)
        )
    }

    fun Map<Filename, FileData>.replaceData(fileData: FileData) : Map<Filename, FileData> {
        return mapValues { entry ->
            if (entry.key == fileData.path) {
                fileData
            } else {
                entry.value
            }
        }
    }

    fun FileData.addToTagCache(tagCache: Map<Tag, Set<Filename>>) : Map<Tag, Set<Filename>> {
        return tags
            .associateWith { path }
            .mapValues { entry ->
                setOf(entry.value).plus(tagCache[entry.key] ?: emptySet())
            }
    }

    fun FileData.addToDataviewCache(dataviewCache: Map<Pair<DataviewField, DataviewValue>, Set<Filename>>)
    : Map<Pair<DataviewField, DataviewValue>, Set<Filename>> {
        return dataviewCache
    }

    fun FileData.addToBacklinkCache(backlinkCache: Map<Filename, Set<Filename>>) : Map<Filename, Set<Filename>> {
        return backlinkCache
    }
}}