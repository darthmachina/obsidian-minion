package app.minion.core.model

data class Note(
    val content: String,
    val fileInfo: ListItemFileInfo,
    val subnotes: List<Note> = emptyList()
)
