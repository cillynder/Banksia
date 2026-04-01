package moe.lava.banksia.core.room

import androidx.room.RoomDatabase
import org.koin.core.parameter.ParametersHolder
import org.koin.core.scope.Scope

class IosDatabaseBuilder() : PlatformDatabaseBuilder {
    override fun getBuilder(): RoomDatabase.Builder<Database> {
        TODO("Not yet implemented")
    }
}

internal actual fun Scope.provideDatabaseBuilder(p: ParametersHolder): PlatformDatabaseBuilder =
    IosDatabaseBuilder()
