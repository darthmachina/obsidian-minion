package app.minion.core.store

import app.minion.core.MinionError
import app.minion.core.model.*
import arrow.core.Either
import arrow.core.raise.either

interface StateFunctions { companion object {
    fun Map<Filename, FileData>.replaceData(fileData: FileData) : Map<Filename, FileData> {
        return mapValues { entry ->
            if (entry.key == fileData.name) {
                fileData
            } else {
                entry.value
            }
        }
    }

    fun FileData.replaceTasks(tasks: List<Task>) : List<Task> {
        return tasks
            .filter { task -> task.fileInfo.file != this.name }
            .plus(this.tasks)
    }

    fun FileData.addToTagCache(tagCache: Map<Tag, Set<Filename>>) : Map<Tag, Set<Filename>> {
        return tagCache.plus(tags
            .associateWith { name }
            .mapValues { entry ->
                setOf(entry.value).plus(tagCache[entry.key] ?: emptySet())
            }
        )
    }

    fun FileData.addToDataviewCache(dataviewCache: Map<Pair<DataviewField, DataviewValue>, Set<Filename>>)
    : Map<Pair<DataviewField, DataviewValue>, Set<Filename>> {
        return dataviewCache.plus(dataview
            .map { entry -> Pair(entry.key, entry.value) }
            .associateWith { name }
            .mapValues { entry ->
                setOf(entry.value).plus(dataviewCache[entry.key] ?: emptySet())
            }
        )
    }

    fun FileData.addToBacklinkCache(backlinkCache: Map<Filename, Set<Filename>>) : Map<Filename, Set<Filename>> {
        return backlinkCache.plus(outLinks
            .associateWith { name }
            .mapValues { entry ->
                setOf(entry.key).plus(backlinkCache[entry.value] ?: emptySet())
            }
        )
    }
}}
