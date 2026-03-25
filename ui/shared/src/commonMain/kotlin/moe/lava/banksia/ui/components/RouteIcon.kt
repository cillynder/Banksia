package moe.lava.banksia.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import moe.lava.banksia.model.RouteType
import moe.lava.banksia.model.RouteType.MetroBus
import moe.lava.banksia.model.RouteType.MetroTrain
import moe.lava.banksia.model.RouteType.MetroTram
import moe.lava.banksia.ui.extensions.getUIProperties
import org.jetbrains.compose.resources.painterResource

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
internal fun RouteIconPreview() {
    Row {
        RouteIcon(routeType = MetroTrain)
        RouteIcon(routeType = MetroTram)
        RouteIcon(routeType = MetroBus)
    }
}

