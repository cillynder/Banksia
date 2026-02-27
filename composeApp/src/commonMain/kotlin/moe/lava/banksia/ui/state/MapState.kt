package moe.lava.banksia.ui.state

import moe.lava.banksia.ui.utils.map.Marker
import moe.lava.banksia.ui.utils.map.Polyline

data class MapState(
    val stops: List<Marker.Stop> = listOf(),
    val vehicles: List<Marker.Vehicle> = listOf(),
    val polylines: List<Polyline> = listOf(),
)
