package moe.lava.banksia.ui.map.mappers

import moe.lava.banksia.core.util.Point
import org.maplibre.spatialk.geojson.Position

internal fun Point.toPosition() = Position(lng, lat)
