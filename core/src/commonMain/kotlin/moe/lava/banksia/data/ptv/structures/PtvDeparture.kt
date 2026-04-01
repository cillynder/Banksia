package moe.lava.banksia.data.ptv.structures

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PtvDeparture(
    @SerialName("scheduled_departure_utc") val scheduledDepartureUtc: String,
    @SerialName("estimated_departure_utc") val estimatedDepartureUtc: String?,
    @SerialName("direction_id") val directionId: Int,
    @SerialName("route_id") val routeId: Int,
)
