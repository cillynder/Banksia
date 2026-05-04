package moe.lava.banksia.core.data.repositories

import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate
import moe.lava.banksia.core.data.sources.stoptime.StopTimeLocalDataSource

actual class StopTimeRepository internal constructor(
    private val local: StopTimeLocalDataSource,
) {
    actual suspend fun getForStop(id: String, date: LocalDate) = flow {
        emit(local.getAtStop(id, date))
    }
}
