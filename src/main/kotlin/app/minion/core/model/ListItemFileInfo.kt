package app.minion.core.model

data class ListItemFileInfo(
    val file: Filename,
    val line: Int,
    val original: String
)
