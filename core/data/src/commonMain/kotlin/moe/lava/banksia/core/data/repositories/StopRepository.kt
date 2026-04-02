package moe.lava.banksia.core.data.repositories

import moe.lava.banksia.core.model.Stop

interface StopRepository {
    suspend fun get(id: String): Stop
    suspend fun getByRoute(id: String): List<Stop>
}
