package moe.lava.banksia.core.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Service(
    val id: String,
    val days: List<DayOfWeek>,
    val start: LocalDate,
    val end: LocalDate,
)
