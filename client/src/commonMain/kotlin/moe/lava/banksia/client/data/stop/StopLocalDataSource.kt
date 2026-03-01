package moe.lava.banksia.client.data.stop

import moe.lava.banksia.model.Stop
import moe.lava.banksia.room.dao.RouteDao
import moe.lava.banksia.room.dao.StopDao
import moe.lava.banksia.room.entity.asEntity

class StopLocalDataSource(private val dao: StopDao, private val routeDao: RouteDao) {
    suspend fun get(id: String) = dao.get(id)
    suspend fun getByRoute(id: String) = routeDao.stops(id)
    suspend fun save(vararg stops: Stop) = dao.insertOrReplaceAll(*stops.map { it.asEntity() }.toTypedArray())
}
