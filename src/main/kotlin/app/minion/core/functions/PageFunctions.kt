package app.minion.core.functions

import app.minion.core.MinionError
import app.minion.core.model.DataviewField
import app.minion.core.model.DataviewValue
import arrow.core.Either
import arrow.core.raise.either

interface PageFunctions { companion object {
    fun String.replaceDataviewValue(field: DataviewField, oldValue: DataviewValue, newValue: DataviewValue)
    : Either<MinionError, String> = either {
        val old = "${field.v}:: ${oldValue.v}"
        val updated = "${field.v}:: ${newValue.v}"
        this@replaceDataviewValue
            .replace(old, updated)
    }
}}
