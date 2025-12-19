package moe.lava.banksia.model

import kotlinx.serialization.Serializable

@Serializable
data class VersionMetadata(
    val type: String,
    val lastUpdated: Long,
)
