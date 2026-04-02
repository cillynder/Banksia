package moe.lava.banksia.core.data.sources.route

import moe.lava.banksia.core.model.Route
import moe.lava.banksia.core.room.dao.RouteDao
import moe.lava.banksia.core.room.entity.asEntity

internal class RouteLocalDataSource(private val dao: RouteDao) {
    suspend fun get(id: String) = dao.get(id)
    suspend fun getAll() = dao.getAll()
    suspend fun save(vararg routes: Route) = dao.insertOrReplaceAll(*routes.map { it.asEntity() }.toTypedArray())
}
