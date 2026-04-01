package moe.lava.banksia.ui.map.util

import moe.lava.banksia.core.util.Point

data class CameraPosition(
    val centre: Point = Point(-37.8136, 144.9631),
    val bounds: CameraPositionBounds? = null,
)
