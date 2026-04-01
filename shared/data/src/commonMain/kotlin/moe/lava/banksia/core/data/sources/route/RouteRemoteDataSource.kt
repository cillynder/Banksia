package moe.lava.banksia.core.data.sources.route

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import moe.lava.banksia.model.Route

internal class RouteRemoteDataSource(val client: HttpClient) {
    suspend fun get(id: String) = client.get("routes/${id}").body<Route>()
    suspend fun getAll() = client.get("routes").body<List<Route>>()
}
