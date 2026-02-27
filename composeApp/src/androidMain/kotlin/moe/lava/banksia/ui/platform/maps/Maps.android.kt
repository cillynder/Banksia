@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package moe.lava.banksia.ui.platform.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import moe.lava.banksia.model.RouteType
import moe.lava.banksia.ui.components.getUIProperties
import moe.lava.banksia.ui.platform.BanksiaTheme
import moe.lava.banksia.ui.screens.MELBOURNE
import moe.lava.banksia.ui.screens.MapScreenEvent
import moe.lava.banksia.ui.state.MapState
import moe.lava.banksia.util.BoxedValue
import moe.lava.banksia.util.Point
import moe.lava.banksia.util.log
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.case
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.convertToString
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.switch
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.geojson.dsl.addFeature
import org.maplibre.spatialk.geojson.dsl.buildFeatureCollection

import org.maplibre.compose.camera.CameraPosition as MLCameraPosition
import org.maplibre.spatialk.geojson.Point as MLPoint

fun Point.toPos(): Position = Position(this.lng, this.lat)

@Composable
private fun checkLocationPermission() =
    ContextCompat.checkSelfPermission(LocalContext.current, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

@Composable
actual fun getScreenHeight(): Int {
    val dp = LocalConfiguration.current.screenHeightDp.dp
    return with(LocalDensity.current) {
        dp.roundToPx()
    }
}

@Serializable
data class MarkerProps(
    val type: RouteType,
)

private fun buildMarkers(markers: List<Marker>): FeatureCollection<MLPoint, MarkerProps> {
    return buildFeatureCollection {
        markers.forEach { marker ->
            val type = when (marker) {
                is Marker.Stop -> marker.type
                is Marker.Vehicle -> marker.type
            }
            val id = when (marker) {
                is Marker.Stop -> marker.id
                is Marker.Vehicle -> marker.ref
            }
            addFeature(
                geometry = MLPoint(marker.point.toPos()),
                properties = MarkerProps(type),
            ) {
                setId(id)
            }
        }
    }
}

private val colorTypeExpression @Composable get() = switch(
    input = feature["type"].convertToString(),
    cases = RouteType.entries.map {
        case(label = it.name, output = const(it.getUIProperties().colour))
    }.toTypedArray(),
    fallback = const(BanksiaTheme.colors.surface),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun Maps(
    modifier: Modifier,
    state: MapState,
    onEvent: (MapScreenEvent) -> Unit,
    cameraPositionFlow: Flow<BoxedValue<CameraPosition>>,
    setLastKnownLocation: (Point) -> Unit,
    extInsets: WindowInsets,
) {
    val camPos = rememberCameraState(
        MLCameraPosition(
            zoom = 16.0,
            target = MELBOURNE.toPos()
        )
    )
    val newCameraPos by cameraPositionFlow.collectAsStateWithLifecycle(null)
    LaunchedEffect(newCameraPos) {
        log("maps", "newPos ${newCameraPos?.value}")
        val pos = newCameraPos?.value ?: return@LaunchedEffect
        if (pos.bounds != null) {
            val (northeast, southwest) = pos.bounds
            camPos.animateTo(
                boundingBox = BoundingBox(
                    southwest.toPos(),
                    northeast.toPos()
                )
            )
        } else {
            camPos.animateTo(MLCameraPosition(
                target = pos.centre.toPos(),
                zoom = 16.0,
            ))
        }
    }

    val ctx = LocalContext.current
    val fusedLocation = remember { LocationServices.getFusedLocationProviderClient(ctx) }
    LaunchedEffect(Unit) {
        @SuppressLint("MissingPermission")
        fusedLocation.lastLocation.addOnSuccessListener {
            if (it != null) {
                camPos.position = MLCameraPosition(
                    zoom = 16.0,
                    target = Position(it.longitude, it.latitude)
                )
                setLastKnownLocation(Point(it.latitude, it.longitude))
            }
        }
    }

    MaplibreMap(
        modifier = modifier,
        baseStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/positron"),
        cameraState = camPos,
        options = MapOptions(
            ornamentOptions = OrnamentOptions(
                padding = WindowInsets.safeDrawing.add(extInsets).asPaddingValues(),
                isScaleBarEnabled = false,
                isAttributionEnabled = false,
            )
        )
    ) {
        if (state.stops.isNotEmpty()) {
            val stopsSource = rememberGeoJsonSource(
                GeoJsonData.Features(buildMarkers(state.stops))
            )
            CircleLayer(
                id = "maps-stops0",
                source = stopsSource,
                color = const(BanksiaTheme.colors.surface),
                radius = const(3.dp),
                strokeWidth = const(2.dp),
                strokeColor = colorTypeExpression,
                onClick = { features ->
                    val feature = features[0]
                    val marker = Json.decodeFromJsonElement<MarkerProps>(feature.properties!!)
                    onEvent(MapScreenEvent.SelectStop(marker.type to feature.id!!.content))
                    ClickResult.Consume
                }
            )
        }

        // TODO
//        if (state.vehicles.isNotEmpty()) {
//            val stopsSource = rememberGeoJsonSource(
//                GeoJsonData.Features(buildMarkers(state.vehicles))
//            )
//            SymbolLayer
//            CircleLayer(
//                id = "maps-vehicles0",
//                source = stopsSource,
//                color = const(BanksiaTheme.colors.surface),
//                radius = const(3.dp),
//                strokeWidth = const(2.dp),
//                strokeColor = colorTypeExpression,
//                onClick = { features ->
//                    val feature = features[0]
//                    val marker = Json.decodeFromJsonElement<MarkerProps>(feature.properties!!)
//                    onEvent(MapScreenEvent.SelectStop(marker.type to feature.id!!.content))
//                    ClickResult.Consume
//                }
//            )
//        }
//
//        if (state.polylines.isNotEmpty()) {
//            val polySource = rememberGeoJsonSource(
//
//            )
//            LineLayer(
//                id = "maps-routeline",
//                source = polySource,
//                color = colorTypeExpression,
//            )
//        }
    }
}
