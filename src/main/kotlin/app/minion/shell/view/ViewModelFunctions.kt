package app.minion.shell.view

import app.minion.core.MinionError
import arrow.core.Either
import arrow.core.raise.either
import arrow.core.toOption

interface ViewModelFunctions { companion object {
    fun Item.getPropertyValue(type: PropertyType) : Either<MinionError, String> = either {
        this@getPropertyValue
            .properties
            .find { it.type == type }
            .toOption()
            .toEither {
                MinionError.FieldMissingError("Property of type $type does not exist")
            }
            .map { it.value }
            .bind()
    }
}}