package moe.lava.banksia.client.repository

import moe.lava.banksia.client.data.stoptime.StopTimeLocalDataSource
import moe.lava.banksia.client.data.stoptime.StopTimeRemoteDataSource
import moe.lava.banksia.model.StopTimeDated

class StopTimeRepository(
    private val local: StopTimeLocalDataSource,
    private val remote: StopTimeRemoteDataSource,
) {
    suspend fun getForStop(id: String): List<StopTimeDated> {
        return local
            .getAtStop(id)
            .ifEmpty { remote.getAtStop(id) }
    }
}
