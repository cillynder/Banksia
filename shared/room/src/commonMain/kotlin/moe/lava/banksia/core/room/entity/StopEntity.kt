package moe.lava.banksia.core.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.SET_NULL
import androidx.room.PrimaryKey
import moe.lava.banksia.model.Stop
import moe.lava.banksia.util.Point

@Entity(
    "Stop",
    foreignKeys = [
        ForeignKey(
            StopEntity::class,
            parentColumns = ["id"],
            childColumns = ["parent"],
            onDelete = SET_NULL,
            deferred = true,
        ),
    ]
)
data class StopEntity(
    @PrimaryKey val id: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    @ColumnInfo(index = true) val parent: String?,
    val hasWheelChairBoarding: Boolean,
    val level: String,
    val platformCode: String,
) {
    fun asModel() = Stop(id, name, Point(lat, lng), parent, hasWheelChairBoarding, level, platformCode)
}

fun Stop.asEntity() = StopEntity(id, name, pos.lat, pos.lng, parent, hasWheelChairBoarding, level, platformCode)
