package moe.lava.banksia.client.repository

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import moe.lava.banksia.client.data.stop.StopLocalDataSource
import moe.lava.banksia.client.data.stop.StopRemoteDataSource

class StopRepository(
    private val local: StopLocalDataSource,
    private val remote: StopRemoteDataSource,
) {
    private val mutex = Mutex()

    suspend fun get(id: String) = mutex.withLock { local.get(id)?.asModel() ?: remote.get(id) }
    suspend fun getByRoute(id: String) = mutex.withLock {
        local
            .getByRoute(id)
            .map { it.asModel() }
            .ifEmpty { null }
            ?: remote.getByRoute(id)
    }
}
