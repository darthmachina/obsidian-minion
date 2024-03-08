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
    val groupByOptions: GroupByOptions = GroupByOptions.NONE,
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
    due
}

@Serializable
enum class CodeBlockOptions {
    notes_on_cover
}
