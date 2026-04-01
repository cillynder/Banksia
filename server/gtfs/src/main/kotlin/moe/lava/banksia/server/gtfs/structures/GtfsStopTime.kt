package moe.lava.banksia.server.gtfs.structures

import kotlinx.serialization.Serializable
import moe.lava.banksia.core.model.FutureTime

@Suppress("PropertyName")
@Serializable
internal data class GtfsStopTime(
    val trip_id: String,
    val arrival_time: String,
    val departure_time: String,
    val stop_id: String,
    val stop_sequence: Int,
    val stop_headsign: String,
    val pickup_type: Int,
    val drop_off_type: Int,
    val shape_dist_traveled: String,
) {
    companion object {
        fun parseGtfsTime(time: String): FutureTime {
            val (hour, minute, second) = time.split(":").map { it.toInt() }
            return FutureTime.from(hour, minute, second)
        }
    }
}
