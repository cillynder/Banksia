package moe.lava.banksia.core.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class StopTime<T: TimeType>(
    val patternId: Long,
    val stopId: String,
    val time: T,
    val pickupType: Int,
    val dropOffType: Int,
) {
    typealias Dated = StopTime<TimeType.Dated>
    typealias Undated = StopTime<TimeType.Undated>
}

@Serializable
sealed class TimeType {
    @Serializable
    data class Undated(
        val arrival: FutureTime,
        val departure: FutureTime,
    ) : TimeType()

    @Serializable
    data class Dated(
        val arrival: LocalDateTime,
        val departure: LocalDateTime,
    ) : TimeType()
}

fun StopTime<TimeType.Undated>.atDate(date: LocalDate) = StopTime(
    patternId = patternId,
    stopId = stopId,
    time = TimeType.Dated(
        arrival = time.arrival.atDate(date),
        departure = time.departure.atDate(date),
    ),
    pickupType = pickupType,
    dropOffType = dropOffType,
)
