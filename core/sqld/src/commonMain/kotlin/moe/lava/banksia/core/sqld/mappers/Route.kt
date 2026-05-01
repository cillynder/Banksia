package moe.lava.banksia.core.sqld.mappers

import moe.lava.banksia.core.model.Route
import moe.lava.banksia.core.model.RouteType
import moe.lava.banksia.core.sqld.Route as DbRoute

fun DbRoute.asModel() = Route(
    id = id,
    type = RouteType.from(type.toInt()),
    number = number,
    name = name,
)

fun Route.asDb() = DbRoute(id, type.value.toLong(), number, name)
