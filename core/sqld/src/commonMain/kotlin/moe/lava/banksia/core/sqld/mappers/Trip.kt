package moe.lava.banksia.core.sqld.mappers

import moe.lava.banksia.core.model.Service
import moe.lava.banksia.core.model.Trip
import moe.lava.banksia.core.sqld.Trip as DbTrip

fun DbTrip.asModel(service: Service): Trip {
    if (serviceId != service.id) {
        throw IllegalArgumentException("trip and service id mismatch (${serviceId} != ${service.id})")
    }
    return Trip(
        id = id,
        routeId = routeId,
        service = service,
        shapeId = shapeId,
        tripHeadsign = tripHeadsign,
        directionId = directionId,
        blockId = blockId,
        wheelchairAccessible = wheelchairAccessible == 1L
    )
}

fun Trip.asDb() = DbTrip(
    id = id,
    routeId = routeId,
    serviceId = service.id,
    shapeId = shapeId,
    tripHeadsign = tripHeadsign,
    directionId = directionId,
    blockId = blockId,
    wheelchairAccessible = if (wheelchairAccessible) 1L else 0L
)
