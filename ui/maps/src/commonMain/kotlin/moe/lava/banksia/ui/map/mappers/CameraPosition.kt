package moe.lava.banksia.ui.map.mappers

import moe.lava.banksia.ui.map.util.CameraPosition
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.compose.camera.CameraPosition as MLCameraPosition

internal fun CameraPosition.toMapPosition() = Pair(
    MLCameraPosition(target = this.centre.toPosition(), zoom = 16.0),
    this.bounds?.let {
        BoundingBox(
            southwest = it.southwest.toPosition(),
            northeast = it.northeast.toPosition(),
        )
    }
)
