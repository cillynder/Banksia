package moe.lava.banksia.core.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import moe.lava.banksia.model.Route
import moe.lava.banksia.model.RouteType

@Entity("Route")
data class RouteEntity(
    @PrimaryKey val id: String,
    val type: RouteType,
    val number: String?,
    val name: String,
) {
    fun asModel() = Route(id, type, number, name)
}

fun Route.asEntity() = RouteEntity(id, type, number, name)
