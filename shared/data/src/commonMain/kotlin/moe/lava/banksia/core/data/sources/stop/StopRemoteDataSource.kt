package moe.lava.banksia.core.data.sources.stop

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import moe.lava.banksia.model.Stop

internal class StopRemoteDataSource(val client: HttpClient) {
    suspend fun get(id: String) = client.get("stops/${id}").body<Stop>()
    suspend fun getByRoute(id: String) = client.get("route_stops/${id}").body<List<Stop>>()
}
