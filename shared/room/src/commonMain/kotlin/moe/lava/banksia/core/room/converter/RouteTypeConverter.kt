package moe.lava.banksia.core.room.converter

import androidx.room.TypeConverter
import moe.lava.banksia.model.RouteType

object RouteTypeConverter {
    @TypeConverter
    fun from(value: Int) = RouteType.from(value)

    @TypeConverter
    fun to(routeType: RouteType) = routeType.value
}
