package moe.lava.banksia.core.data.repositories

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import moe.lava.banksia.core.data.sources.stop.StopLocalDataSource
import moe.lava.banksia.core.data.sources.stop.StopRemoteDataSource

class StopRepository internal constructor(
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
