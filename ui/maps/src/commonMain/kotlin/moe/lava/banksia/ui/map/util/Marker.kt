package moe.lava.banksia.ui.map.util

import kotlinx.serialization.Serializable
import moe.lava.banksia.model.RouteType
import moe.lava.banksia.util.Point

@Serializable
sealed class Marker {
    abstract val point: Point

    sealed class Typed : Marker() {
        abstract val type: RouteType
    }

    @Serializable
    data class Stop(
        override val point: Point,
        override val type: RouteType,
        val id: String,
    ) : Typed()

    @Serializable
    data class Vehicle(
        override val point: Point,
        override val type: RouteType,
        val ref: String,
    ) : Typed()
}
