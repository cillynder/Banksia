package moe.lava.banksia.core.sqld

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val sqldDiModule = module {
    singleOf(::DatabaseManager)
    factory { get<DatabaseManager>().database }
    factory { get<BanksiaDatabase>().routeQueries }
    factory { get<BanksiaDatabase>().serviceQueries }
    factory { get<BanksiaDatabase>().serviceExceptionQueries }
    factory { get<BanksiaDatabase>().shapeQueries }
    factory { get<BanksiaDatabase>().stopQueries }
    factory { get<BanksiaDatabase>().stoppingPatternQueries }
    factory { get<BanksiaDatabase>().stopTimeQueries }
    factory { get<BanksiaDatabase>().tripQueries }
}
