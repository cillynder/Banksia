package moe.lava.banksia.core.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import moe.lava.banksia.model.Trip

@Entity(
    "Trip",
    foreignKeys = [
        ForeignKey(RouteEntity::class, parentColumns = ["id"], childColumns = ["routeId"], onDelete = CASCADE),
        ForeignKey(ServiceEntity::class, parentColumns = ["id"], childColumns = ["serviceId"], onDelete = CASCADE),
        ForeignKey(ShapeEntity::class, parentColumns = ["id"], childColumns = ["shapeId"], onDelete = CASCADE),
    ],
    indices = [Index("shapeId"), Index("serviceId")],
)
data class TripEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(index = true) val routeId: String,
    val serviceId: String,
    val shapeId: String?,
    val tripHeadsign: String,
    val directionId: String,
    val blockId: String,
    val wheelchairAccessible: String,
)

fun Trip.Companion.from(tripEntity: TripEntity, serviceEntity: ServiceEntity): Trip {
    if (tripEntity.serviceId != serviceEntity.id) {
        throw IllegalArgumentException("trip and service id mismatch (${tripEntity.serviceId} != ${serviceEntity.id})")
    }
    return with(tripEntity) {
        Trip(
            id = id,
            routeId = routeId,
            service = serviceEntity.asModel(),
            shapeId = shapeId,
            tripHeadsign = tripHeadsign,
            directionId = directionId,
            blockId = blockId,
            wheelchairAccessible = wheelchairAccessible
        )
    }
}

fun Trip.asEntity() = TripEntity(id, routeId, service.id, shapeId, tripHeadsign, directionId, blockId, wheelchairAccessible)
