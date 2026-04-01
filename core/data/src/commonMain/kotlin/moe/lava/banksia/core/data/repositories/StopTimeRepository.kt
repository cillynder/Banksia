package moe.lava.banksia.core.data.repositories

import moe.lava.banksia.core.data.sources.stoptime.StopTimeLocalDataSource
import moe.lava.banksia.core.data.sources.stoptime.StopTimeRemoteDataSource
import moe.lava.banksia.core.model.StopTimeDated

class StopTimeRepository internal constructor(
    private val local: StopTimeLocalDataSource,
    private val remote: StopTimeRemoteDataSource,
) {
    suspend fun getForStop(id: String): List<StopTimeDated> {
        return local
            .getAtStop(id)
            .ifEmpty { remote.getAtStop(id) }
    }
}
