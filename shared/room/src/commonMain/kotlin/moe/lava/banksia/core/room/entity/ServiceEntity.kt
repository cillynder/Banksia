package moe.lava.banksia.core.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import moe.lava.banksia.model.Service
import moe.lava.banksia.util.deserialiseDaysBitflag
import moe.lava.banksia.util.serialise

@Entity("Service")
data class ServiceEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(index = true) val days: Int,
    val start: Int,
    val end: Int,
) {
    fun asModel() = Service(
        id,
        days.deserialiseDaysBitflag(),
        LocalDate.fromEpochDays(start),
        LocalDate.fromEpochDays(end),
    )
}

fun Service.asEntity() = ServiceEntity(
    id,
    days.serialise(),
    start.toEpochDays().toInt(),
    end.toEpochDays().toInt(),
)
