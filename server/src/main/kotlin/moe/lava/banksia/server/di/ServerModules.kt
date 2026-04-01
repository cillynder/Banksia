package moe.lava.banksia.server.di

import io.ktor.client.HttpClient
import moe.lava.banksia.server.GtfsImporter
import moe.lava.banksia.server.gtfs.GtfsParser
import moe.lava.banksia.server.gtfsrt.GtfsrtService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val ServerModules = module {
    single { HttpClient() }
    singleOf(::GtfsParser)
    singleOf(::GtfsrtService)

    singleOf(::GtfsImporter)
}
