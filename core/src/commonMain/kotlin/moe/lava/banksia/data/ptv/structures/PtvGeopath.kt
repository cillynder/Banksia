package moe.lava.banksia.data.ptv.structures

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PtvGeopath(
    @SerialName("direction_id") val directionId: Int,
    @SerialName("valid_from") val validFrom: String,
    @SerialName("valid_to") val validTo: String,
    @SerialName("paths") val paths: List<String>,
)

