package moe.lava.banksia.core.sqld.mappers

import moe.lava.banksia.core.model.Stop
import moe.lava.banksia.core.util.Point
import moe.lava.banksia.core.sqld.Stop as DbStop

fun DbStop.asModel() = Stop(
    id = id,
    name = name,
    pos = Point(lat, lng),
    parent = parent,
    hasWheelChairBoarding = hasWheelChairBoarding == 1L,
    level = level,
    platformCode = platformCode,
)

fun Stop.asDb() = DbStop(
    id = id,
    name = name,
    lat = pos.lat,
    lng = pos.lng,
    parent = parent,
    hasWheelChairBoarding = if (hasWheelChairBoarding) 1L else 0L,
    level = level,
    platformCode = platformCode
)
