package moe.lava.banksia.core.sqld.mappers

import moe.lava.banksia.core.model.Service
import moe.lava.banksia.core.model.StoppingPattern
import moe.lava.banksia.core.model.Trip
import moe.lava.banksia.core.sqld.Trip as DbTrip

fun DbTrip.asModel(pattern: StoppingPattern.Undated, service: Service): Trip.Undated {
    if (serviceId != service.id) {
        throw IllegalArgumentException("trip and service id mismatch (${serviceId} != ${service.id})")
    }
    return Trip(
        id = gtfsId,
        pattern = pattern,
        service = service,
        directionId = directionId.toInt(),
        blockId = blockId.toString(),
    )
}

fun Trip.Undated.asDb() = DbTrip(
    gtfsId = id,
    patternId = pattern.id,
    serviceId = service.id,
    directionId = directionId.toLong(),
    blockId = blockId?.toLong(),
)
