package moe.lava.banksia.core.data.sources.stoptime

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import moe.lava.banksia.core.model.StopTimeDated
import moe.lava.banksia.core.model.atDate
import moe.lava.banksia.core.sqld.StopTimeQueries
import moe.lava.banksia.core.sqld.mappers.asModel
import moe.lava.banksia.core.util.serialise
import kotlin.time.Clock

internal class StopTimeLocalDataSource(
    private val queries: StopTimeQueries,
) {
    suspend fun getAtStop(
        stopId: String,
        date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    ): List<StopTimeDated> {
        return withContext(Dispatchers.IO) {
            queries
                .getForStopDated(
                    listOf(date.dayOfWeek).serialise().toLong(),
                    date.toEpochDays(),
                    stopId,
                )
                .executeAsList()
                .map { it.asModel().atDate(date) }
                .sortedBy { it.departureTime }
        }
    }
}
