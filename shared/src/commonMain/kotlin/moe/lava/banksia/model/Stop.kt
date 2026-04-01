package moe.lava.banksia.model

import kotlinx.serialization.Serializable
import moe.lava.banksia.util.Point

@Serializable
data class Stop(
    val id: String,
    val name: String,
    val pos: Point,
    val parent: String?,
    val hasWheelChairBoarding: Boolean,
    val level: String,
    val platformCode: String,
)
