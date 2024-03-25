package app.minion.core.model

import kotlinx.serialization.Serializable

value class Filename(val v: String)
value class File(val v: String)
value class Content(val v: String)
@Serializable data class Tag(val v: String, val type: TagType = TagType.TASK)
@Serializable value class DataviewField(val v: String)
value class DataviewValue(val v: String)

enum class TagType {
    TASK,
    PAGE
}