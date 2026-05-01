package moe.lava.banksia.core.data.sources.stop

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import moe.lava.banksia.core.model.Stop
import moe.lava.banksia.core.sqld.StopQueries
import moe.lava.banksia.core.sqld.mappers.asDb

internal class StopLocalDataSource(private val queries: StopQueries) {
    suspend fun get(id: String) = withContext(Dispatchers.IO) { queries.get(id).executeAsOneOrNull() }
    suspend fun getByRoute(id: String) = withContext(Dispatchers.IO) { queries.getByRoute(id).executeAsList() }
    suspend fun save(vararg stops: Stop) {
        withContext(Dispatchers.IO) {
            queries.transaction {
                stops.forEach {
                    queries.insert(it.asDb())
                }
            }
        }
    }
}
