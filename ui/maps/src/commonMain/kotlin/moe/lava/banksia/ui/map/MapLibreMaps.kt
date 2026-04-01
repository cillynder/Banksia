package moe.lava.banksia.ui.map

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.JsonObject
import moe.lava.banksia.core.Constants
import moe.lava.banksia.ui.map.mappers.routeColorExpression
import moe.lava.banksia.ui.platform.BanksiaTheme
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Geometry

@Composable
internal fun MapLibreMaps(
    modifier: Modifier,
    insets: WindowInsets,
    positionState: MapsPositionState,
    stops: GeoJsonData.Features?,
//    vehicles: GeoJsonData.Features?,
    stopInnerColor: Color,
    onStopClicked: (Feature<Geometry, JsonObject?>) -> Unit,
) {
    val camPos = rememberCameraState(
        CameraPosition(
            zoom = 16.0,
            target = MELBOURNE_POS
        )
    )

    val variant = if (isSystemInDarkTheme()) "dark" else "light"

    MaplibreMap(
        modifier = modifier,
        baseStyle = BaseStyle.Uri("https://api.protomaps.com/styles/v5/$variant/en.json?key=${Constants.protomapsKey}"),
        cameraState = camPos,
        options = MapOptions(
            ornamentOptions = OrnamentOptions(
                padding = WindowInsets.safeDrawing.add(insets).asPaddingValues(),
                isScaleBarEnabled = false,
                isAttributionEnabled = false,
            )
        )
    ) {
        if (stops != null) {
            val stopsSource = rememberGeoJsonSource(stops)
            CircleLayer(
                id = "maps-stops0",
                source = stopsSource,
                color = const(BanksiaTheme.colors.surface),
                radius = const(3.dp),
                strokeWidth = const(2.dp),
                strokeColor = routeColorExpression,
            )
            CircleLayer(
                id = "maps-stops0-clickhandler",
                source = stopsSource,
                color = const(Color.Transparent),
                radius = const(12.dp),
                onClick = { features ->
//                    onEvent(MapScreenEvent.SelectStop(marker.type to feature.id!!.content))
//                    val marker = Json.decodeFromJsonElement<T>(feature.properties!!)
                    onStopClicked(features[0])
                    ClickResult.Consume
                }
            )
        }
    }
}
