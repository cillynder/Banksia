package moe.lava.banksia.core.model

import kotlinx.serialization.Serializable

@Serializable
data class StopTime(
    val tripId: String,
    val stopId: String,
    val arrivalTime: FutureTime,
    val departureTime: FutureTime,
    val headsign: String?,
    val pickupType: Int,
    val dropOffType: Int,
)
