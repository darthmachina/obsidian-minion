package app.minion.shell.view.codeblock

import app.minion.core.model.Tag
import kotlinx.serialization.Serializable

// NOTE: Enums are lowercase so they can be deserialized easily

@Serializable
data class CodeBlockConfig(
    val heading: String = "",
    val query: CodeBlockQuery,
    val display: CodeBlockDisplay,
    val due: List<DueOptions> = emptyList(),
    val include: IncludeExcludeOptions = IncludeExcludeOptions(),
    val exclude: IncludeExcludeOptions = IncludeExcludeOptions(),
    val properties: List<String> = emptyList(),
    val sort: List<String> = emptyList(),
    val limit: Int = 0,
    val groupBy: GroupByOptions = GroupByOptions.NONE,
    val groupByField: String = "",
    val options: List<CodeBlockOptions> = emptyList()
)

@Serializable
enum class CodeBlockQuery {
    pages,
    tasks,
}

@Serializable
enum class CodeBlockDisplay {
    list,
    gallery,
    kanban,
    table
}

@Serializable
enum class DueOptions {
    today,
    overdue,
    upcoming
}

@Serializable
data class IncludeExcludeOptions(
    val tags: List<Tag> = emptyList(),
    val parentTags: List<String> = emptyList(),
    val links: List<String> = emptyList(),
    val dateCreated: String = "",
    val dateModified: String = "",
    val dataview: List<String> = emptyList()
)

@Serializable
enum class GroupByOptions {
    NONE,
    source,
    parent_tag,
    due,
    dataview
}

@Serializable
enum class CodeBlockOptions {
    notes_on_cover
}

// Hard coded property values
const val PROPERTY_CREATED = "created"
const val PROPERTY_MODIFIED = "modified"
const val PROPERTY_SOURCE = "source"
const val PROPERTY_TAGS = "tags"
const val PROPERTY_DUE = "due"

const val GROUP_BY_SINGLE = "!!single!!"
