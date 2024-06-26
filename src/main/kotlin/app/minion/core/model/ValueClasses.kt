package app.minion.core.model

import kotlinx.serialization.Serializable

value class Filename(val v: String)
value class File(val v: String)
value class Content(val v: String)
@Serializable data class Tag(val v: String, val type: TagType = TagType.TASK) {
    // Override default hashCode and equals to only check the tag itself, not the type
    override fun hashCode(): Int {
        return v.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Tag) return false
        return v == other.v
    }
}
@Serializable value class DataviewField(val v: String)
value class DataviewValue(val v: String) : Comparable<DataviewValue> {
    override fun compareTo(other: DataviewValue): Int {
        val numThis = v.toDoubleOrNull()
        val numOther = other.v.toDoubleOrNull()
        return if (numThis != null && numOther != null) {
            numThis.compareTo(numOther)
        } else {
            v.compareTo(other.v)
        }
    }

}

enum class TagType {
    TASK,
    PAGE
}
