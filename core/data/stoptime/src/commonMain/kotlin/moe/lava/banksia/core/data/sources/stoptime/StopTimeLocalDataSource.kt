package moe.lava.banksia.core.data.sources.stoptime

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import moe.lava.banksia.core.model.StopTime
import moe.lava.banksia.core.model.atDate
import moe.lava.banksia.core.sqld.StopTimeQueries
import moe.lava.banksia.core.sqld.mappers.asModel
import moe.lava.banksia.core.util.serialise
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

internal class StopTimeLocalDataSource : KoinComponent {
    private val queries get() = get<StopTimeQueries>()

    suspend fun getAtStop(stopId: String, date: LocalDate): List<StopTime.Dated> {
        return withContext(context = Dispatchers.IO) {
            queries
                .getForStopDated(
                    listOf(date.dayOfWeek).serialise().toLong(),
                    date.toEpochDays(),
                    stopId,
                )
                .executeAsList()
                .map { it.asModel().atDate(date) }
                .sortedBy { it.time.departure }
        }
    }
}
