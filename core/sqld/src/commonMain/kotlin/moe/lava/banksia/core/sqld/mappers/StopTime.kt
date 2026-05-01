package moe.lava.banksia.core.sqld.mappers

import moe.lava.banksia.core.model.FutureTime
import moe.lava.banksia.core.model.FutureTime.Companion.asInt
import moe.lava.banksia.core.model.StopTime
import moe.lava.banksia.core.sqld.StopTime as DbStopTime

fun DbStopTime.asModel() = StopTime(
    tripId = tripId,
    stopId = stopId,
    arrivalTime = FutureTime.fromInt(arrivalTime.toInt()),
    departureTime = FutureTime.fromInt(departureTime.toInt()),
    headsign = null,
    pickupType = pickupType.toInt(),
    dropOffType = dropOffType.toInt(),
)

fun StopTime.asDb() = DbStopTime(
    tripId = tripId,
    stopId = stopId,
    arrivalTime = arrivalTime.asInt().toLong(),
    departureTime = departureTime.asInt().toLong(),
    pickupType = pickupType.toLong(),
    dropOffType = dropOffType.toLong(),
)
