package app.minion.core.functions

import app.minion.core.model.DataviewField
import app.minion.core.model.DataviewValue
import app.minion.core.model.FileData
import app.minion.core.model.Tag

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
}}
