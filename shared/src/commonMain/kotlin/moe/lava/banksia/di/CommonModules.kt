package moe.lava.banksia.di

import moe.lava.banksia.room.Database
import org.koin.dsl.module

val CommonModules = module {
    includes(PlatformModule)

    single { Database.build(get<PlatformDatabaseBuilder>().getBuilder()) }
    single { get<Database>().versionMetadataDao }
    single { get<Database>().routeDao }
    single { get<Database>().shapeDao }
    single { get<Database>().stopDao }
    single { get<Database>().stopTimeDao }
    single { get<Database>().tripDao }
}
