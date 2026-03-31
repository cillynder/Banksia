package moe.lava.banksia.server.gtfs.structures

import kotlinx.serialization.Serializable

@Suppress("PropertyName")
@Serializable
data class GtfsService(
    val service_id: String,
    val monday: Int,
    val tuesday: Int,
    val wednesday: Int,
    val thursday: Int,
    val friday: Int,
    val saturday: Int,
    val sunday: Int,
    val start_date: String,
    val end_date: String,
)
