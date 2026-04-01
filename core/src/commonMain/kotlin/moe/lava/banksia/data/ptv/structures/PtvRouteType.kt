package moe.lava.banksia.data.ptv.structures

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import moe.lava.banksia.core.model.RouteType

object PtvRouteTypeSerialiser : KSerializer<PtvRouteType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        PtvRouteType::class.qualifiedName!!,
        PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: PtvRouteType) {
        encoder.encodeInt(value.ordinal)
    }

    override fun deserialize(decoder: Decoder): PtvRouteType {
        val index = decoder.decodeInt()
        return PtvRouteType.entries[index]
    }
}

@Serializable(with = PtvRouteTypeSerialiser::class)
enum class PtvRouteType {
    TRAIN,
    TRAM,
    BUS,
    VLINE,
    NIGHT_BUS,
    ;

    companion object {
        fun fromModel(type: RouteType) = when (type) {
            RouteType.MetroTrain -> TRAIN
            RouteType.MetroTram -> TRAM
            RouteType.MetroBus -> BUS
            RouteType.RegionalTrain -> VLINE
            RouteType.RegionalCoach -> BUS
            RouteType.RegionalBus -> BUS
            RouteType.SkyBus -> BUS
            RouteType.Interstate -> TRAIN
        }

        fun RouteType.asPtvType() = fromModel(this)
    }
}
