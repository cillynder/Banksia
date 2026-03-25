package moe.lava.banksia.ui.extensions

import androidx.compose.ui.graphics.Color
import moe.lava.banksia.data.ptv.structures.PtvRouteType
import moe.lava.banksia.model.RouteType
import moe.lava.banksia.resources.Res
import moe.lava.banksia.resources.bus
import moe.lava.banksia.resources.bus_background
import moe.lava.banksia.resources.bus_icon
import moe.lava.banksia.resources.train
import moe.lava.banksia.resources.train_background
import moe.lava.banksia.resources.train_icon
import moe.lava.banksia.resources.tram
import moe.lava.banksia.resources.tram_background
import moe.lava.banksia.resources.tram_icon
import org.jetbrains.compose.resources.DrawableResource

data class RouteTypeProperties(
    val colour: Color,
    val drawable: DrawableResource,
    val background: DrawableResource,
    val icon: DrawableResource,
)

const val TRAIN_BLUE = 0xFF0072CE
const val TRAM_GREEN = 0xFF78BE20
const val BUS_ORANGE = 0xFFFF8200
const val VLINE_PURPLE = 0xFF8F1A95

fun RouteType.getUIProperties(): RouteTypeProperties {
    val colour = when (this) {
        RouteType.MetroTrain -> TRAIN_BLUE
        RouteType.MetroTram -> TRAM_GREEN
        RouteType.MetroBus -> BUS_ORANGE
        RouteType.RegionalTrain -> VLINE_PURPLE
        RouteType.RegionalCoach -> VLINE_PURPLE
        RouteType.RegionalBus -> VLINE_PURPLE
        RouteType.SkyBus -> BUS_ORANGE
        RouteType.Interstate -> BUS_ORANGE
    }

    val (drawable, background, icon) = when (this) {
        RouteType.MetroTrain,
        RouteType.RegionalTrain,
        RouteType.Interstate -> Triple(
            Res.drawable.train, Res.drawable.train_background, Res.drawable.train_icon
        )

        RouteType.MetroTram -> Triple(
            Res.drawable.tram, Res.drawable.tram_background, Res.drawable.tram_icon
        )

        RouteType.MetroBus,
        RouteType.RegionalCoach,
        RouteType.RegionalBus,
        RouteType.SkyBus -> Triple(
            Res.drawable.bus, Res.drawable.bus_background, Res.drawable.bus_icon
        )
    }

    return RouteTypeProperties(Color(colour), drawable, background, icon)
}

fun PtvRouteType.getUIProperties(): RouteTypeProperties {
    val colour = when (this) {
        PtvRouteType.TRAIN -> Color(TRAIN_BLUE)
        PtvRouteType.TRAM -> Color(TRAM_GREEN)
        PtvRouteType.BUS, PtvRouteType.NIGHT_BUS -> Color(BUS_ORANGE)
        PtvRouteType.VLINE -> Color(VLINE_PURPLE)
    }
    val (drawable, background, icon) = when (this) {
        PtvRouteType.TRAM -> Triple(
            Res.drawable.tram, Res.drawable.tram_background, Res.drawable.tram_icon
        )
        PtvRouteType.TRAIN, PtvRouteType.VLINE -> Triple(
            Res.drawable.train, Res.drawable.train_background, Res.drawable.train_icon
        )
        PtvRouteType.BUS, PtvRouteType.NIGHT_BUS -> Triple(
            Res.drawable.bus, Res.drawable.bus_background, Res.drawable.bus_icon
        )
    }
    return RouteTypeProperties(colour, drawable, background, icon)
}

