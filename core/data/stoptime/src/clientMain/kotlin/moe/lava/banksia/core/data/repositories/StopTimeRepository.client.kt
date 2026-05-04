package moe.lava.banksia.core.data.repositories

import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate
import moe.lava.banksia.core.data.sources.stoptime.StopTimeLocalDataSource
import moe.lava.banksia.core.data.sources.stoptime.StopTimeRemoteDataSource

actual class StopTimeRepository internal constructor(
    private val local: StopTimeLocalDataSource,
    private val remote: StopTimeRemoteDataSource,
) {
    actual suspend fun getForStop(id: String, date: LocalDate) = flow {
        emit(local.getAtStop(id, date))

        remote.getAtStop(id, date)
            .takeIf { it.isNotEmpty() }
            ?.let { emit(it) }
    }
}
