package app.minion.core.functions

import app.minion.core.model.DataviewField
import app.minion.core.model.DataviewValue
import app.minion.core.model.FileData
import app.minion.core.model.Tag
import mu.KotlinLogging

private val logger = KotlinLogging.logger("PageFilterFunctions")

interface PageFilterFunctions { companion object {
    fun List<FileData>.filterByTags(tags: List<Tag>) : List<FileData> {
        return this
            .filter { page ->
                page.tags.containsAll(tags)
            }
    }

    fun List<FileData>.filterByAnyTag(tags: List<Tag>) : List<FileData> {
        return this
            .filter { page ->
                page.tags.any { tags.contains(it) }
            }
    }

    fun List<FileData>.filterByDataview(values: List<Pair<DataviewField, DataviewValue>>) : List<FileData> {
        return this
            .filter { page ->
                page.dataview
                    .map { Pair(it.key, it.value) }
                    .containsAll(values)
            }
    }

    fun List<FileData>.filterByAnyDataview(values: List<Pair<DataviewField, DataviewValue>>) : List<FileData> {
        return this
            .filter { page ->
                page.dataview
                    .map { Pair(it.key, it.value) }
                    .any { values.contains(it) }
            }
    }

    fun List<FileData>.filterBySource(values: List<String>) : List<FileData> {
        return this
            .filter { page ->
                values.all { search ->
                    if (search.startsWith("/")) {
                        search
                            .replace("/", "")
                            .toRegex()
                            .matches(page.name.v)
                    } else {
                        values.contains(page.name.v)
                    }
                }
            }
    }

    /**
     * Only includes FileData objects that are outlinks from the given source files.
     */
    fun List<FileData>.filterByOutlinks(values: List<String>) : List<FileData> {
        return this
            .filter { fileData -> values.contains(fileData.name.v) }
            .flatMap { it.outLinks }
            .let { outlinks ->
                logger.info { "outlinks: $outlinks" }
                this
                    .filter { fileData ->
                        outlinks.contains(fileData.name)
                    }
            }
    }
}}
