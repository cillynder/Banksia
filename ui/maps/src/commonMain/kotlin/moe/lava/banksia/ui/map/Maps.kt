package moe.lava.banksia.ui.map

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import moe.lava.banksia.core.util.Point
import moe.lava.banksia.ui.map.mappers.asFeatures
import moe.lava.banksia.ui.map.mappers.toPosition
import moe.lava.banksia.ui.map.util.Marker
import moe.lava.banksia.ui.platform.BanksiaTheme

internal val MELBOURNE = Point(-37.8136, 144.9631)
internal val MELBOURNE_POS = MELBOURNE.toPosition()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Maps(
    modifier: Modifier = Modifier,
    insets: WindowInsets = WindowInsets(),
    stops: List<Marker.Stop> = listOf(),
//    vehicles: List<Marker.Vehicle> = listOf(),
    positionState: MapsPositionState = rememberMapsPositionState(),
    onStopClicked: (id: String) -> Unit = {},
//    onVehicleClicked: (id: String) -> Unit = {},
) {
    MapLibreMaps(
        modifier = modifier,
        insets = insets,
        positionState = positionState,
        stops = stops.takeIf { it.isNotEmpty() }?.asFeatures(),
//        vehicles = vehicles.takeIf { it.isNotEmpty() }?.asFeatures(),
        stopInnerColor = BanksiaTheme.colors.surface,
        onStopClicked = { feature -> onStopClicked(feature.id!!.content) },
//        onVehicleClicked = {},
    )
}
