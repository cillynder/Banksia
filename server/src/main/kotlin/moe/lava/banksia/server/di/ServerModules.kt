package moe.lava.banksia.server.di

import io.ktor.client.HttpClient
import moe.lava.banksia.core.room.roomDiModule
import moe.lava.banksia.server.GtfsDataFixer
import moe.lava.banksia.server.GtfsImporter
import moe.lava.banksia.server.gtfs.GtfsParser
import moe.lava.banksia.server.gtfsrt.GtfsrtService
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val ServerModules = module {
    includes(roomDiModule)

    single { HttpClient() }
    singleOf(::GtfsParser)
    singleOf(::GtfsrtService)

    factoryOf(::GtfsDataFixer)
    factoryOf(::GtfsImporter)
}
