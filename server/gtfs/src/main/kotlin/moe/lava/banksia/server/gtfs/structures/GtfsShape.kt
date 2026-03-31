package moe.lava.banksia.server.gtfs.structures

import kotlinx.serialization.Serializable

@Suppress("PropertyName")
@Serializable
internal data class GtfsShape(
    val shape_id: String,
    val shape_pt_lat: Double,
    val shape_pt_lon: Double,
    val shape_pt_sequence: Int,
    val shape_dist_traveled: String,
)
