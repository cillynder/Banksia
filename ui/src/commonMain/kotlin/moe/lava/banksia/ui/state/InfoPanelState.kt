package moe.lava.banksia.ui.state

import moe.lava.banksia.model.RouteType

sealed class InfoPanelState {
    abstract val loading: Boolean

    data object None : InfoPanelState() {
        override val loading = false
    }

    data class Route(
        val name: String,
        val type: RouteType,
    ) : InfoPanelState() {
        override val loading = false
    }

    data class Run(
        val direction: String,
        val type: RouteType,
        val routeName: String? = null,
    ) : InfoPanelState() {
        override val loading = routeName == null
    }

    data class Stop(
        val id: String,
        val name: String,
        val subname: String? = null,
        val departures: List<Departure>? = null,
    ) : InfoPanelState() {
        override val loading: Boolean
            get() = departures == null

        data class Departure(val directionName: String, val formattedTimes: String)
    }
}
