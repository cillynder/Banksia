package moe.lava.banksia.data.ptv.structures

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

// Some datetimes are in local time (no timezone), observed on bus vehicle positions,
// and some datetimes are in UTC, observed on train vehicle positions. We need to handle
// both cases.
private object CustomInstantSerialiser : KSerializer<Instant> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor(
            CustomInstantSerialiser::class.qualifiedName!!,
            PrimitiveKind.STRING,
        )

    override fun serialize(
        encoder: Encoder,
        value: Instant
    ) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Instant {
        val str = decoder.decodeString()
        return runCatching {
            Instant.parse(str)
        }.getOrElse {
            LocalDateTime.parse(str).toInstant(TimeZone.currentSystemDefault())
        }
    }
}

@Serializable
data class PtvVehiclePosition(
    val latitude: Double,
    val longitude: Double,
    val easting: Double?,
    val northing: Double?,
    val direction: String?,
    val bearing: Double?,
    val supplier: String?,

    @Serializable(CustomInstantSerialiser::class)
    @SerialName("datetime_utc")
    val datetimeUtc: Instant?,

    @Serializable(CustomInstantSerialiser::class)
    @SerialName("expiry_time")
    val expiryTime: Instant?,
)

@Serializable
data class PtvRun(
    @SerialName("run_ref") val runRef: String,
    @SerialName("route_id") val routeId: Int,
    @SerialName("route_type") val routeType: PtvRouteType,
    @SerialName("final_stop_id") val finalStopId: Int,
    @SerialName("destination_name") val destinationName: String,
    @SerialName("direction_id") val directionId: Int,
    @SerialName("status") val status: String,
    @SerialName("vehicle_position") val vehiclePosition: PtvVehiclePosition?,
)
