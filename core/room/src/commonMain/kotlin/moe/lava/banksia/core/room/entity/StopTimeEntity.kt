package moe.lava.banksia.core.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import kotlinx.serialization.ExperimentalSerializationApi
import moe.lava.banksia.core.model.FutureTime
import moe.lava.banksia.core.model.FutureTime.Companion.asInt
import moe.lava.banksia.core.model.StopTime

@Entity(
    "StopTime",
    primaryKeys = ["tripId", "stopId"],
    indices = [
        Index("tripId", unique = false),
        Index("stopId", unique = false),
    ],
    foreignKeys = [
        ForeignKey(TripEntity::class, parentColumns = ["id"], childColumns = ["tripId"], onDelete = CASCADE),
        ForeignKey(StopEntity::class, parentColumns = ["id"], childColumns = ["stopId"], onDelete = CASCADE),
    ]
)
data class StopTimeEntity(
    val tripId: String,
    val stopId: String,
    val arrivalTime: Int,
    val departureTime: Int,
    val headsign: String?,
    val pickupType: Int,
    val dropOffType: Int,
) {
    fun asModel() = StopTime(
        tripId,
        stopId,
        FutureTime.fromInt(arrivalTime),
        FutureTime.fromInt(departureTime),
        headsign,
        pickupType,
        dropOffType,
    )
}

@OptIn(ExperimentalSerializationApi::class)
fun StopTime.asEntity() = StopTimeEntity(
    tripId,
    stopId,
    arrivalTime.asInt(),
    departureTime.asInt(),
    headsign,
    pickupType,
    dropOffType,
)
