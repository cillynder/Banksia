package moe.lava.banksia.core.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class StopTimeDated(
    val tripId: String,
    val stopId: String,
    val arrivalTime: LocalDateTime,
    val departureTime: LocalDateTime,
    val headsign: String?,
    val pickupType: Int,
    val dropOffType: Int,
)

fun StopTime.atDate(date: LocalDate) = StopTimeDated(
    tripId = tripId,
    stopId = stopId,
    arrivalTime = arrivalTime.atDate(date),
    departureTime = departureTime.atDate(date),
    headsign = headsign,
    pickupType = pickupType,
    dropOffType = dropOffType,
)
