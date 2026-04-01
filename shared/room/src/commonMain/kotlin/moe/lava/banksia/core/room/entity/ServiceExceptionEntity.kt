package moe.lava.banksia.core.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import kotlinx.datetime.LocalDate
import moe.lava.banksia.model.ServiceException

@Entity(
    "ServiceException",
    primaryKeys = ["serviceId", "date"]
)
data class ServiceExceptionEntity(
    @ColumnInfo(index = true) val serviceId: String,
    val date: Int,
    @ColumnInfo(index = true) val type: Int,
) {
    fun asModel() = ServiceException(
        serviceId,
        LocalDate.fromEpochDays(date),
        type,
    )
}

fun ServiceException.asEntity() = ServiceExceptionEntity(
    serviceId,
    date.toEpochDays().toInt(),
    type,
)
