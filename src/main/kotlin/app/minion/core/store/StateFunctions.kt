package app.minion.core.store

import app.minion.core.model.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger("StateFunctions")

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

    fun List<Task>.replaceTasks(fileData: FileData) : List<Task> {
        return this
            .filter { task -> task.fileInfo.file != fileData.name }
            .plus(fileData.tasks)
    }

    fun FileData.updateTagCache(tagCache: Map<Tag, Set<Filename>>) : Map<Tag, Set<Filename>> {
        return tagCache
            .mapValues { entry ->
                entry.value.minus(name)
            }
            .plus(tags
                .associateWith { name }
                .mapValues { entry ->
                    setOf(entry.value).plus(tagCache[entry.key] ?: emptySet())
                }
            )
    }

    fun FileData.updateDataviewCache(dataviewCache: Map<Pair<DataviewField, DataviewValue>, Set<Filename>>)
    : Map<Pair<DataviewField, DataviewValue>, Set<Filename>> {
        return dataviewCache
            .mapValues { entry ->
                entry.value.minus(name)
            }
            .plus(dataview
                .map { entry -> Pair(entry.key, entry.value) }
                .associateWith { name }
                .mapValues { entry ->
                    setOf(entry.value).plus(dataviewCache[entry.key] ?: emptySet())
                }
            )
    }

    fun FileData.updateBacklinkCache(backlinkCache: Map<Filename, Set<Filename>>) : Map<Filename, Set<Filename>> {
        return backlinkCache
            .mapValues { entry ->
                entry.value.minus(name)
            }
            .plus(outLinks
                .associateWith { name }
                .mapValues { entry ->
                    logger.info { "${entry.key} to ${entry.value}" }
                    setOf(entry.value).plus(backlinkCache[entry.key] ?: emptySet())
                }
            )
    }
}}
