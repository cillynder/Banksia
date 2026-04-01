package moe.lava.banksia.core.data.sources.stop

import moe.lava.banksia.core.model.Stop
import moe.lava.banksia.core.room.dao.RouteDao
import moe.lava.banksia.core.room.dao.StopDao
import moe.lava.banksia.core.room.entity.asEntity

internal class StopLocalDataSource(private val dao: StopDao, private val routeDao: RouteDao) {
    suspend fun get(id: String) = dao.get(id)
    suspend fun getByRoute(id: String) = routeDao.stops(id)
    suspend fun save(vararg stops: Stop) = dao.insertOrReplaceAll(*stops.map { it.asEntity() }.toTypedArray())
}
