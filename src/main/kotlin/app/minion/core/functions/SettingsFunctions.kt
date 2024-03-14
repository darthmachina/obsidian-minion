package app.minion.core.functions

import app.minion.core.MinionError
import app.minion.core.model.MinionSettings1
import app.minion.core.model.MinionSettings2
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
    : Either<MinionError, MinionSettings2> = either {
        logger.debug { "loadFromJson" }
        maybeJson.fold(
            ifEmpty = {
                MinionSettings2.default()
            },
            ifSome = { json ->
                runCatching {
                    when(jsonSerializer.decodeFromString<SettingsVersion>(json).version) {
                        "2" -> {
                            logger.debug { "Version 2, just loading" }
                            jsonSerializer.decodeFromString<MinionSettings2>(json)
                        }
                        "1" -> {
                            logger.debug { "Version 1, upgrading" }
                            jsonSerializer
                                .decodeFromString<MinionSettings1>(json)
                                .upgradeTo2()
                        }
                        else -> raise(MinionError.LoadSettingsError("Cannot load settings for JSON: $json"))
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

    fun MinionSettings1.upgradeTo2() : MinionSettings2 {
        logger.debug { "MinionSettings1.upgradeTo2()" }
        return MinionSettings2.default().copy(
            lifeAreas = this.lifeAreas
        )
    }

    fun MinionSettings1.toJson() : String {
        return jsonSerializer.encodeToString(this)
    }
}}