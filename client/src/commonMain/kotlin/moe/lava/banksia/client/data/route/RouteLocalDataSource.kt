package moe.lava.banksia.client.data.route

import moe.lava.banksia.model.Route
import moe.lava.banksia.room.dao.RouteDao
import moe.lava.banksia.room.entity.asEntity

class RouteLocalDataSource(private val dao: RouteDao) {
    suspend fun get(id: String) = dao.get(id)
    suspend fun getAll() = dao.getAll()
    suspend fun save(vararg routes: Route) = dao.insertOrReplaceAll(*routes.map { it.asEntity() }.toTypedArray())
}
