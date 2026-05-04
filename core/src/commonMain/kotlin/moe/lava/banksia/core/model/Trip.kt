package moe.lava.banksia.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Trip<T: TimeType>(
    val id: String,
    val pattern: StoppingPattern<T>,
    val service: Service,
    val directionId: Int,
    val blockId: String?,
) {
    typealias Dated = Trip<TimeType.Dated>
    typealias Undated = Trip<TimeType.Undated>
}
