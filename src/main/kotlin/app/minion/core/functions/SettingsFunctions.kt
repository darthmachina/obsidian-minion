package app.minion.core.functions

import app.minion.core.MinionError
import app.minion.core.model.MinionSettings1
import app.minion.core.model.SettingsVersion
import arrow.core.Either
import arrow.core.Option
import arrow.core.raise.either
import arrow.core.toOption
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging

private val logger = KotlinLogging.logger("SettingsFunctions")

interface SettingsFunctions { companion object {
    private val jsonSerializer = Json { ignoreUnknownKeys = true }

    fun loadFromJson(maybeJson: Option<String>)
    : Either<MinionError, MinionSettings1> = either {
        logger.debug { "loadFromJson" }
        maybeJson.fold(
            ifEmpty = {
                MinionSettings1.default()
            },
            ifSome = { json ->
                runCatching {
                    when(jsonSerializer.decodeFromString<SettingsVersion>(json).version) {
                        "1" -> {
                            logger.debug { "Version 1, just loading" }
                            jsonSerializer
                                .decodeFromString<MinionSettings1>(json)
                        }
                        else -> {
                            raise(MinionError.LoadSettingsError("Cannot load settings for JSON: $json"))
                        }
                    }
                }
                    .getOrElse {
                        raise(
                            MinionError.LoadSettingsError("Cannot read JSON: $json", it.toOption())
                        )
                    }
           }
        )
    }

    fun MinionSettings1.toJson() : String {
        return jsonSerializer.encodeToString(this)
    }
}}