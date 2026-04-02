package moe.lava.banksia.core.data.repositories

import moe.lava.banksia.core.model.StopTimeDated

interface StopTimeRepository {
    suspend fun getForStop(id: String): List<StopTimeDated>
}
