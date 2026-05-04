package moe.lava.banksia.core.model

import kotlinx.serialization.Serializable

@Serializable
data class StoppingPattern<T: TimeType>(
    val id: Long,
    val routeId: String,
    val shapeId: String,
    val headsign: String,
    val wheelchairAccessible: Boolean,
    val stoptimes: List<StopTime<T>>,
) {
    typealias Dated = StoppingPattern<TimeType.Dated>
    typealias Undated = StoppingPattern<TimeType.Undated>
}
