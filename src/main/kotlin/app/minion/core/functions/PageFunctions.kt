package app.minion.core.functions

import app.minion.core.MinionError
import app.minion.core.model.DataviewField
import app.minion.core.model.DataviewValue
import arrow.core.Either
import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.raise.either

interface PageFunctions { companion object {
    fun String.upsertDataviewValue(field: DataviewField, oldValue: Option<DataviewValue>, newValue: DataviewValue)
    : Either<MinionError, String> = either {
        val updated = "${field.v}:: ${newValue.v}"
        oldValue.map {
            val old = "${field.v}:: ${it.v}"
            this@upsertDataviewValue
                .replace(old, updated)
        }.getOrElse {
            val old = "${field.v}::"
            if (this@upsertDataviewValue.indexOf(old) > 0) {
                this@upsertDataviewValue.replace(old, updated)
            } else {
                "$updated\n${this@upsertDataviewValue}"
            }
        }
    }
}}
