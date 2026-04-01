package moe.lava.banksia.core.model

import kotlinx.serialization.Serializable
import moe.lava.banksia.core.util.Point

typealias ShapePath = List<Point>

@Serializable
data class Shape(
    val id: String,
    val path: ShapePath,
)
