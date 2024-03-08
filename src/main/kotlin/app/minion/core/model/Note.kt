package app.minion.core.model

data class Note(
    val content: Content,
    val fileInfo: ListItemFileInfo,
    val subnotes: List<Note> = emptyList()
)
