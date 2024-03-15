package app.minion.core.model

data class ListItemFileInfo(
    val file: Filename,
    val path: File,
    val line: Int,
    val original: String
)
