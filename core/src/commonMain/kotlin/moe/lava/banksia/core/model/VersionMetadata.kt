package moe.lava.banksia.core.model

import kotlinx.serialization.Serializable

@Serializable
data class VersionMetadata(
    val type: String,
    val lastUpdated: Long,
)
