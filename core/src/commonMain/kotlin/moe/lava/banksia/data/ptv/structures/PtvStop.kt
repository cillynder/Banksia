package moe.lava.banksia.data.ptv.structures

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PtvStop(
    @SerialName("stop_id") val stopId: Int,
    @SerialName("stop_name") val stopName: String,
    @SerialName("stop_latitude") val stopLatitude: Double?,
    @SerialName("stop_longitude") val stopLongitude: Double?,
    @SerialName("route_type") val routeType: PtvRouteType,
)
