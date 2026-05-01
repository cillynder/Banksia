package moe.lava.banksia.core.data.sources.route

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import moe.lava.banksia.core.model.Route
import moe.lava.banksia.core.sqld.RouteQueries
import moe.lava.banksia.core.sqld.mappers.asDb

internal class RouteLocalDataSource(private val queries: RouteQueries) {
    suspend fun get(id: String) = withContext(Dispatchers.IO) { queries.get(id).executeAsOneOrNull() }
    suspend fun getAll() = withContext(Dispatchers.IO) { queries.getAll().executeAsList() }
    suspend fun save(vararg routes: Route) {
        withContext(Dispatchers.IO) {
            queries.transaction {
                routes.forEach {
                    queries.insert(it.asDb())
                }
            }
        }
    }
}
