package moe.lava.banksia.core.sqld.mappers

import moe.lava.banksia.core.model.StopTime
import moe.lava.banksia.core.model.StoppingPattern
import moe.lava.banksia.core.sqld.StoppingPattern as DbStoppingPattern

fun DbStoppingPattern.asModel(stoptimes: List<StopTime.Undated>) = StoppingPattern.Undated(
    id = id,
    routeId = routeId,
    shapeId = shapeId,
    headsign = headsign,
    wheelchairAccessible = wheelchairAccessible == 1L,
    stoptimes = stoptimes,
)

fun StoppingPattern.Undated.asDb() = DbStoppingPattern(
    id = id,
    routeId = routeId,
    shapeId = shapeId,
    headsign = headsign,
    wheelchairAccessible = if (wheelchairAccessible) 1L else 0L,
)
