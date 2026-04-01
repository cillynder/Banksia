package moe.lava.banksia.core.data.sources.stoptime

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import moe.lava.banksia.core.room.dao.StopTimeDao
import moe.lava.banksia.model.StopTimeDated
import moe.lava.banksia.model.atDate
import moe.lava.banksia.util.serialise
import kotlin.time.Clock

internal class StopTimeLocalDataSource(
    private val stopTimeDao: StopTimeDao,
) {
    suspend fun getAtStop(
        stopId: String,
        date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    ): List<StopTimeDated> {
        return stopTimeDao
            .getForStopDated(
                stopId,
                listOf(date.dayOfWeek).serialise(),
                date.toEpochDays().toInt(),
            )
            .map { it.asModel().atDate(date) }
            .sortedBy { it.departureTime }
    }
}
