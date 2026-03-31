package moe.lava.banksia.server.gtfs.structures

import kotlinx.serialization.Serializable

@Suppress("PropertyName")
@Serializable
internal data class GtfsRoute(
    val route_id: String,
    val agency_id: String,
    val route_short_name: String,
    val route_long_name: String,
    val route_type: String,
    val route_color: String,
    val route_text_color: String,
)
