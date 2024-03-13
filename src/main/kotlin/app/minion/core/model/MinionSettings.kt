package app.minion.core.model

import io.kvision.core.Color
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


typealias MinionSettings = MinionSettings1

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

object ColorAsStringSerializer : KSerializer<Color> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Color", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Color) {
        encoder.encodeString(value.asString())
    }

    override fun deserialize(decoder: Decoder): Color {
        return Color(decoder.decodeString())
    }
}
