package moe.lava.banksia.server.gtfs.structures

import kotlinx.serialization.Serializable

@Suppress("PropertyName")
@Serializable
internal data class GtfsTrip(
    val route_id: String,
    val service_id: String,
    val trip_id: String,
    val shape_id: String,
    val trip_headsign: String,
    val direction_id: String,
    val block_id: String,
    val wheelchair_accessible: String,
)
