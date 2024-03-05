package app.minion.core.model

data class FileData(
    val path: Filename,
    val title: PageTitle,
    val tags: List<Tag> = emptyList()
)
