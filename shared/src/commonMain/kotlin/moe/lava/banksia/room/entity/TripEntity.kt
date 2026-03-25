package moe.lava.banksia.room.entity

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
        ForeignKey(ShapeEntity::class, parentColumns = ["id"], childColumns = ["shapeId"], onDelete = CASCADE),
    ],
    indices = [Index("shapeId")],
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
) {
    fun asModel() = Trip(id, routeId, serviceId, shapeId, tripHeadsign, directionId, blockId, wheelchairAccessible)
}

fun Trip.asEntity() = TripEntity(id, routeId, serviceId, shapeId, tripHeadsign, directionId, blockId, wheelchairAccessible)
