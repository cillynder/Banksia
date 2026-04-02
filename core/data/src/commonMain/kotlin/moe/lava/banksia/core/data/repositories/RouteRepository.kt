package moe.lava.banksia.core.data.repositories

import moe.lava.banksia.core.model.Route

interface RouteRepository {
    suspend fun get(id: String): Route
    suspend fun getAll(): List<Route>
}
