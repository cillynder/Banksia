package moe.lava.banksia.core.sqld.mappers

import kotlinx.datetime.LocalDate
import moe.lava.banksia.core.model.ServiceException
import moe.lava.banksia.core.sqld.ServiceException as DbServiceException

fun DbServiceException.asModel() = ServiceException(
    serviceId = serviceId,
    date = LocalDate.fromEpochDays(date),
    type = type.toInt(),
)

fun ServiceException.asDb() = DbServiceException(
    serviceId = serviceId,
    type = date.toEpochDays(),
    date = type.toLong(),
)
