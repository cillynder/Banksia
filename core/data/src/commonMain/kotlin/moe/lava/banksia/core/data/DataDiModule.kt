package moe.lava.banksia.core.data

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.plugin
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import moe.lava.banksia.core.Constants
import moe.lava.banksia.core.data.repositories.RouteRepository
import moe.lava.banksia.core.data.repositories.StopRepository
import moe.lava.banksia.core.data.repositories.StopTimeRepository
import moe.lava.banksia.core.data.sources.route.RouteLocalDataSource
import moe.lava.banksia.core.data.sources.route.RouteRemoteDataSource
import moe.lava.banksia.core.data.sources.stop.StopLocalDataSource
import moe.lava.banksia.core.data.sources.stop.StopRemoteDataSource
import moe.lava.banksia.core.data.sources.stoptime.StopTimeLocalDataSource
import moe.lava.banksia.core.data.sources.stoptime.StopTimeRemoteDataSource
import moe.lava.banksia.core.room.roomDiModule
import moe.lava.banksia.core.util.log
import moe.lava.banksia.data.ptv.PtvService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataDiModule = module {
    includes(roomDiModule)

    // HTTP Clients
    singleOf(::PtvService)
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
            defaultRequest {
                url(Constants.serverUrl)
            }
        }.also { client ->
            client.plugin(HttpSend).intercept { req ->
                val fullPath = req.url.build().encodedPathAndQuery
                log("ktor.client", fullPath)
                execute(req)
            }
        }
    }

    // Data sources
    singleOf(::RouteLocalDataSource)
    singleOf(::RouteRemoteDataSource)
    singleOf(::StopLocalDataSource)
    singleOf(::StopRemoteDataSource)
    singleOf(::StopTimeLocalDataSource)
    singleOf(::StopTimeRemoteDataSource)

    // Repositories
    singleOf(::RouteRepository)
    singleOf(::StopRepository)
    singleOf(::StopTimeRepository)
}
