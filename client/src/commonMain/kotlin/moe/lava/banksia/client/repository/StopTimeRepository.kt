package moe.lava.banksia.client.repository

import moe.lava.banksia.client.data.stoptime.StopTimePtvDataSource
import moe.lava.banksia.model.StopTime

class StopTimeRepository(
    private val ptv: StopTimePtvDataSource,
) {
    // TODO
    suspend fun getForStop(id: String): List<StopTime> {
        return listOf()
    }
}
