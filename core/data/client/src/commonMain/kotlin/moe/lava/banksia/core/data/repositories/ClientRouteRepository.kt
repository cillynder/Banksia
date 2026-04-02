package moe.lava.banksia.core.data.repositories

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import moe.lava.banksia.core.data.sources.route.RouteLocalDataSource
import moe.lava.banksia.core.data.sources.route.RouteRemoteDataSource

internal class ClientRouteRepository internal constructor(
    private val local: RouteLocalDataSource,
    private val remote: RouteRemoteDataSource,
) : RouteRepository {
    private val mutex = Mutex()
    override suspend fun getAll() = mutex.withLock {
        local
            .getAll()
            .map { it.asModel() }
            .ifEmpty {
                remote
                    .getAll()
                    .also { local.save(*it.toTypedArray()) }
            }
    }

    override suspend fun get(id: String) = mutex.withLock { local.get(id)?.asModel() ?: remote.get(id) }
}
