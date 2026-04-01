package moe.lava.banksia.core.room

import androidx.room.RoomDatabase
import org.koin.core.parameter.ParametersHolder
import org.koin.core.scope.Scope
import org.koin.dsl.module

val roomDiModule = module {
    single { provideDatabaseBuilder(it) }
    single { Database.build(get<PlatformDatabaseBuilder>().getBuilder()) }

    single { get<Database>().versionMetadataDao }
    single { get<Database>().routeDao }
    single { get<Database>().serviceDao }
    single { get<Database>().serviceExceptionDao }
    single { get<Database>().shapeDao }
    single { get<Database>().stopDao }
    single { get<Database>().stopTimeDao }
    single { get<Database>().tripDao }
}

internal interface PlatformDatabaseBuilder {
    fun getBuilder(): RoomDatabase.Builder<Database>
}

internal expect fun Scope.provideDatabaseBuilder(p: ParametersHolder): PlatformDatabaseBuilder
