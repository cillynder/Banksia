package moe.lava.banksia.model

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import moe.lava.banksia.model.FutureTime.Companion.asInt

@Serializable(FutureTimeSerialiser::class)
data class FutureTime(
    val dayOffset: Boolean,
    val time: LocalTime,
) {
    companion object {
        fun from(hour: Int, minute: Int, second: Int): FutureTime {
            var nHour = hour
            val nextDay = hour >= 24
            if (nextDay)
                nHour -= 24
            val time = LocalTime(nHour, minute, second)
            return FutureTime(nextDay, time)
        }

        fun FutureTime.asInt() =
            trueHour * 3600 + minute * 60 + second

        fun fromInt(int: Int) = from(
            int / 3600,
            (int / 60) % 60,
            int % 60,
        )
    }

    val hour = time.hour
    val minute = time.minute
    val second = time.second
    val trueHour = time.hour + (if (dayOffset) 24 else 0)

    fun atDate(date: LocalDate) = date
        .let { if (dayOffset) date.plus(1, DateTimeUnit.DAY) else date }
        .atTime(time)
}

object FutureTimeSerialiser: KSerializer<FutureTime> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(FutureTimeSerialiser::class.qualifiedName!!, PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: FutureTime) = encoder.encodeInt(value.asInt())
    override fun deserialize(decoder: Decoder) = FutureTime.fromInt(decoder.decodeInt())
}
