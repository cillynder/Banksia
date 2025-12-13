package moe.lava.banksia.server.di

import io.ktor.client.HttpClient
import moe.lava.banksia.server.gtfs.GtfsHandler
import moe.lava.banksia.server.gtfsr.GtfsrService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val ServerModules = module {
    single { HttpClient() }
    singleOf(::GtfsHandler)
    singleOf(::GtfsrService)
}
