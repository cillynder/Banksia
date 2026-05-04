package moe.lava.banksia.core.sqld.mappers

import moe.lava.banksia.core.model.FutureTime
import moe.lava.banksia.core.model.FutureTime.Companion.asInt
import moe.lava.banksia.core.model.StopTime
import moe.lava.banksia.core.model.TimeType
import moe.lava.banksia.core.sqld.StopTime as DbStopTime

fun DbStopTime.asModel() = StopTime(
    patternId = patternId,
    stopId = stopId,
    time = TimeType.Undated(
        arrival = FutureTime.fromInt((departureTime + arrivalDelta).toInt()),
        departure = FutureTime.fromInt(departureTime.toInt()),
    ),
    pickupType = pickupType.toInt(),
    dropOffType = dropOffType.toInt(),
)

fun StopTime.Undated.asDb() = DbStopTime(
    patternId = patternId,
    stopId = stopId,
    arrivalDelta = (time.arrival.asInt() - time.departure.asInt()).toLong(),
    departureTime = time.departure.asInt().toLong(),
    pickupType = pickupType.toLong(),
    dropOffType = dropOffType.toLong(),
)
