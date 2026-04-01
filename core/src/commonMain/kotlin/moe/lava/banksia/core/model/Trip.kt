package moe.lava.banksia.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Trip(
    val id: String,
    val routeId: String,
    val service: Service,
    val shapeId: String?,
    val tripHeadsign: String,
    val directionId: String,
    val blockId: String,
    val wheelchairAccessible: String,
)
