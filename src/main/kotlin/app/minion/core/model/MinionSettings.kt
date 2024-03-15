package app.minion.core.model

import app.minion.core.functions.ColorAsStringSerializer
import app.minion.core.functions.LoggingLevelSerializer
import io.kvision.core.Color
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import mu.KotlinLoggingLevel

typealias MinionSettings = MinionSettings2

@Serializable
data class MinionSettings2(
    val version: String,
    @Serializable(with = LoggingLevelSerializer::class) val logLevel: KotlinLoggingLevel,
    val lifeAreas: Map<String, @Serializable(ColorAsStringSerializer::class)Color>,
    val excludeFolders: Set<String>
) { companion object {
    fun default() : MinionSettings2 {
        return MinionSettings2(
            "2",
            KotlinLoggingLevel.DEBUG,
            mapOf(
                "personal" to Color("#13088C"),
                "home" to Color("#460A60"),
                "marriage" to Color("#196515"),
                "family" to Color("#8E791C"),
                "work" to Color("#D34807")
            ),
            setOf()
        )
    }}}

@Serializable
data class MinionSettings1(
    val version: String,
    val lifeAreas: Map<String, @Serializable(ColorAsStringSerializer::class)Color>
) { companion object {
    fun default() : MinionSettings1 {
        return MinionSettings1(
            "1",
            mapOf(
                "personal" to Color("#13088C"),
                "home" to Color("#460A60"),
                "marriage" to Color("#196515"),
                "family" to Color("#8E791C"),
                "work" to Color("#D34807")
            )
        )
    }}}

@Serializable
data class SettingsVersion(
    val version: String
)

