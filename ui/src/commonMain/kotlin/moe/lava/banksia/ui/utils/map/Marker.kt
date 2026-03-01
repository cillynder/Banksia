package moe.lava.banksia.ui.utils.map

import androidx.compose.ui.graphics.Color
import moe.lava.banksia.model.RouteType
import moe.lava.banksia.util.Point

sealed class Marker {
    abstract val point: Point

    data class Stop(
        override val point: Point,
        val id: String,
        val type: RouteType,
        val colour: Color,
    ) : Marker()

    data class Vehicle(
        override val point: Point,
        val ref: String,
        val type: RouteType,
    ) : Marker()
}
