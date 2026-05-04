package moe.lava.banksia.core.data

import moe.lava.banksia.core.data.sources.stoptime.StopTimeLocalDataSource
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal expect val platformModule: Module;

val stopTimeDataDiModule = module {
    includes(platformModule)
    singleOf(::StopTimeLocalDataSource)
}
