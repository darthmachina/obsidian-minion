package app.minion.core.model

data class FileData(
    val name: Filename,
    val path: File,
    val tags: List<Tag> = emptyList(),
    val outLinks: List<Filename> = emptyList(),
    val dataview: Map<DataviewField, DataviewValue> = emptyMap(),
    val tasks: List<Task> = emptyList()
)
