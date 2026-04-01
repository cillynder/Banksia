package moe.lava.banksia.server.gtfs.structures

import kotlinx.serialization.Serializable

@Suppress("PropertyName")
@Serializable
internal data class GtfsServiceException(
    val service_id: String,
    val date: String,
    val exception_type: Int,
)
