package app.minion.core.model

enum class RepeatSpan(val span: String) {
    DAILY("daily"),
    WEEKLY("weekly"),
    MONTHLY("monthly"),
    YEARLY("yearly"),
    WEEKDAY("weekday"),
   UNKNOWN("unknown");

    companion object {
        fun getAllSpans(): List<String> {
            return values().map { it.span }
        }

        fun findForSpan(span: String): RepeatSpan {
            return values().find { it.span == span } ?: UNKNOWN
        }
    }
}

data class RepeatInfo(
    val value: Int,
    val span: RepeatSpan,
    val afterComplete: Boolean
) {
    fun asString(): String {
        return "$span : $value${if (afterComplete) " after completed" else ""}"
    }
}
