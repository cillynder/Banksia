package moe.lava.banksia.core.data.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import moe.lava.banksia.core.model.StopTime
import kotlin.time.Clock

expect class StopTimeRepository {
    suspend fun getForStop(
        id: String,
        date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    ): Flow<List<StopTime.Dated>>
}
