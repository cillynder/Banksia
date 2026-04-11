package moe.lava.banksia.core.room

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val roomDiModule = module {
    singleOf(::DatabaseManager)
    factory { get<DatabaseManager>().database }

    factory { get<Database>().versionMetadataDao }
    factory { get<Database>().routeDao }
    factory { get<Database>().serviceDao }
    factory { get<Database>().serviceExceptionDao }
    factory { get<Database>().shapeDao }
    factory { get<Database>().stopDao }
    factory { get<Database>().stopTimeDao }
    factory { get<Database>().tripDao }
}
