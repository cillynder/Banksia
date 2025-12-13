package moe.lava.banksia.di

import androidx.room.Room
import androidx.room.RoomDatabase
import moe.lava.banksia.room.Database
import org.koin.core.parameter.ParametersHolder
import org.koin.core.scope.Scope
import org.koin.dsl.module
import java.io.File

class JvmDatabaseBuilder() : PlatformDatabaseBuilder {
    override fun getBuilder(): RoomDatabase.Builder<Database> {
        val dbFile = File("./data/room.db")
        return Room.databaseBuilder<Database>(
            name = dbFile.absolutePath,
        )
    }
}

actual fun Scope.provideDatabaseBuilder(p: ParametersHolder): PlatformDatabaseBuilder =
    JvmDatabaseBuilder()

internal actual val ExtPlatformModule = module {  }
