package moe.lava.banksia.core.sqld.mappers

import kotlinx.datetime.LocalDate
import moe.lava.banksia.core.model.Service
import moe.lava.banksia.core.util.deserialiseDaysBitflag
import moe.lava.banksia.core.util.serialise
import moe.lava.banksia.core.sqld.Service as DbService

fun DbService.asModel() = Service(
    id = id,
    days = days.toInt().deserialiseDaysBitflag(),
    start = LocalDate.fromEpochDays(start),
    end = LocalDate.fromEpochDays(end),
)

fun Service.asDb() = DbService(
    id = id,
    days = days.serialise().toLong(),
    start = start.toEpochDays(),
    end = end.toEpochDays(),
)
