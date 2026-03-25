package moe.lava.banksia.ui.map.mappers

import kotlinx.serialization.Serializable
import moe.lava.banksia.model.RouteType
import moe.lava.banksia.ui.map.util.Marker
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.dsl.addFeature
import org.maplibre.spatialk.geojson.dsl.buildFeatureCollection
import org.maplibre.spatialk.geojson.Point as MLPoint

@Serializable
data class MarkerProps(
    val type: RouteType,
)

@Suppress("NOTHING_TO_INLINE")
internal inline fun Iterable<Marker>.asFeatures() = GeoJsonData.Features(asFeatureCollection())

internal fun Iterable<Marker>.asFeatureCollection(): FeatureCollection<MLPoint, MarkerProps> {
    val markers = this
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
                geometry = MLPoint(marker.point.toPosition()),
                properties = MarkerProps(type),
            ) {
                setId(id)
            }
        }
    }
}
