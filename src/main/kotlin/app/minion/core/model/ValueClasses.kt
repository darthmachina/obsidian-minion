package app.minion.core.model

import kotlinx.serialization.Serializable

value class Filename(val v: String) {
    fun fullName() : String { return "$v.md" }
}
value class Content(val v: String)
@Serializable value class Tag(val v: String)
value class DataviewField(val v: String)
value class DataviewValue(val v: String)
