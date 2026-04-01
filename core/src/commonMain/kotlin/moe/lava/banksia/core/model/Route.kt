package moe.lava.banksia.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Route(
    val id: String,
    val type: RouteType,
    val number: String?,
    val name: String,
)
