package app.minion.core.model

data class FileData(
    val path: Filename,
    val tags: List<Tag> = emptyList(),
    val outLinks: List<Filename> = emptyList(),
    val dataview: Map<DataviewField, DataviewValue> = emptyMap()
)
