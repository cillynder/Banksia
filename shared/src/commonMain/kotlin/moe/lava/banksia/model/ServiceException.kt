package moe.lava.banksia.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class ServiceException(
    val serviceId: String,
    val date: LocalDate,
    val type: Int,
)
