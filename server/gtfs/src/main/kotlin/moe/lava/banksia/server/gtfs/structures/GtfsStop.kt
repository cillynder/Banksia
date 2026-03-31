package moe.lava.banksia.server.gtfs.structures

import kotlinx.serialization.Serializable

@Suppress("PropertyName")
@Serializable
internal data class GtfsStop(
    val stop_id: String,
    val stop_name: String,
    val stop_lat: Double,
    val stop_lon: Double,
    val location_type: String,
    val parent_station: String,
    val wheelchair_boarding: String,
    val level_id: String,
    val platform_code: String,
)
