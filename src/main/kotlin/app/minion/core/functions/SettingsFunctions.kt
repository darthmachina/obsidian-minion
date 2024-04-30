package app.minion.core.functions

import app.minion.core.MinionError
import app.minion.core.model.MinionSettings
import app.minion.core.model.MinionSettings1
import app.minion.core.model.MinionSettings2
import app.minion.core.model.MinionSettings3
import app.minion.core.model.SettingsVersion
import arrow.core.Either
import arrow.core.Option
import arrow.core.raise.either
import arrow.core.toOption
import io.kvision.core.Color
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import mu.KotlinLoggingLevel

private val logger = KotlinLogging.logger("SettingsFunctions")

interface SettingsFunctions { companion object {
    private val jsonSerializer = Json { ignoreUnknownKeys = true }

    fun loadFromJson(maybeJson: Option<String>)
    : Either<MinionError, MinionSettings3> = either {
        logger.debug { "loadFromJson" }
        maybeJson.fold(
            ifEmpty = {
                MinionSettings3.default()
            },
            ifSome = { json ->
                runCatching {
                    when(jsonSerializer.decodeFromString<SettingsVersion>(json).version) {
                        "3" -> {
                            logger.debug { "Version 3, just loading" }
                            jsonSerializer.decodeFromString<MinionSettings3>(json)
                        }
                        "2" -> {
                            logger.debug { "Version 2, upgrading" }
                            jsonSerializer
                                .decodeFromString<MinionSettings2>(json)
                                .upgradeTo3()
                        }
                        "1" -> {
                            logger.debug { "Version 1, upgrading" }
                            jsonSerializer
                                .decodeFromString<MinionSettings1>(json)
                                .upgradeTo2()
                                .upgradeTo3()
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

    fun MinionSettings2.upgradeTo3() : MinionSettings3 {
        logger.debug { "MinionSettings2.upgradeTo3()" }
        return MinionSettings3.default().copy(
            lifeAreas = this.lifeAreas,
            logLevel = this.logLevel,
            excludeFolders = this.excludeFolders,
            pageTaskFields = this.pageTaskFields
        )
    }

    fun MinionSettings1.upgradeTo2() : MinionSettings2 {
        logger.debug { "MinionSettings1.upgradeTo2()" }
        return MinionSettings2.default().copy(
            lifeAreas = this.lifeAreas
        )
    }

    fun MinionSettings.toJson() : String {
        return jsonSerializer.encodeToString(this)
    }
}}

object LoggingLevelSerializer : KSerializer<KotlinLoggingLevel> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LogLevel", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): KotlinLoggingLevel {
        return KotlinLoggingLevel.valueOf(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: KotlinLoggingLevel) {
        encoder.encodeString(value.name)
    }
}

object ColorAsStringSerializer : KSerializer<Color> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Color", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Color) {
        encoder.encodeString(value.asString())
    }

    override fun deserialize(decoder: Decoder): Color {
        return Color(decoder.decodeString())
    }
}
