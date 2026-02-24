package moe.lava.banksia.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import moe.lava.banksia.data.ptv.structures.PtvRouteType
import moe.lava.banksia.model.RouteType
import moe.lava.banksia.model.RouteType.Interstate
import moe.lava.banksia.model.RouteType.MetroBus
import moe.lava.banksia.model.RouteType.MetroTrain
import moe.lava.banksia.model.RouteType.MetroTram
import moe.lava.banksia.model.RouteType.RegionalBus
import moe.lava.banksia.model.RouteType.RegionalCoach
import moe.lava.banksia.model.RouteType.RegionalTrain
import moe.lava.banksia.model.RouteType.SkyBus
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
import org.jetbrains.compose.resources.painterResource

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
        MetroTrain -> TRAIN_BLUE
        MetroTram -> TRAM_GREEN
        MetroBus -> BUS_ORANGE
        RegionalTrain -> VLINE_PURPLE
        RegionalCoach -> VLINE_PURPLE
        RegionalBus -> VLINE_PURPLE
        SkyBus -> BUS_ORANGE
        Interstate -> BUS_ORANGE
    }

    val (drawable, background, icon) = when (this) {
        MetroTrain,
        RegionalTrain,
        Interstate -> Triple(
            Res.drawable.train, Res.drawable.train_background, Res.drawable.train_icon
        )

        MetroTram -> Triple(
            Res.drawable.tram, Res.drawable.tram_background, Res.drawable.tram_icon
        )

        MetroBus,
        RegionalCoach,
        RegionalBus,
        SkyBus -> Triple(
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

@Composable
fun RouteIcon(
    modifier: Modifier = Modifier.Companion,
    size: Dp = 40.dp,
    routeType: RouteType,
) {
    val properties = routeType.getUIProperties()
    Image(
        painter = painterResource(properties.icon),
        contentDescription = null,
        modifier = modifier
            .size(size)
            .aspectRatio(1f)
            .padding(size * ICON_PADDING / 2)
            .drawBehind {
                drawCircle(properties.colour, radius = size.toPx() / 2f)
            }
    )
}

const val ICON_PADDING = 0.25f

@Preview
@Composable
private fun RouteIconPreview() {
    Row {
        RouteIcon(routeType = MetroTrain)
        RouteIcon(routeType = MetroTram)
        RouteIcon(routeType = MetroBus)
    }
}

