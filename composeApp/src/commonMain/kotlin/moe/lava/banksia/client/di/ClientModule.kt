package moe.lava.banksia.client.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.plugin
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import moe.lava.banksia.Constants
import moe.lava.banksia.client.datasource.local.RouteLocalDataSource
import moe.lava.banksia.client.datasource.local.StopLocalDataSource
import moe.lava.banksia.client.datasource.remote.RouteRemoteDataSource
import moe.lava.banksia.client.datasource.remote.StopRemoteDataSource
import moe.lava.banksia.client.repository.RouteRepository
import moe.lava.banksia.client.repository.StopRepository
import moe.lava.banksia.data.ptv.PtvService
import moe.lava.banksia.ui.screens.map.MapScreenViewModel
import moe.lava.banksia.util.log
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val ClientModule = module {
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

    // Repositories
    singleOf(::RouteRepository)
    singleOf(::StopRepository)

    // ViewModel
    viewModelOf(::MapScreenViewModel)
}
