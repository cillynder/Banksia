package moe.lava.banksia.di

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import moe.lava.banksia.room.Database
import org.koin.core.parameter.ParametersHolder
import org.koin.core.scope.Scope
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

class IosDatabaseBuilder() : PlatformDatabaseBuilder {
    @OptIn(ExperimentalForeignApi::class)
    override fun getBuilder(): RoomDatabase.Builder<Database> {
        val path = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        val dbPath = path!!.path + "/room.db"
        return Room.databaseBuilder<Database>(
            name = dbPath
        )
    }
}

actual fun Scope.provideDatabaseBuilder(p: ParametersHolder): PlatformDatabaseBuilder =
    IosDatabaseBuilder()

internal actual val ExtPlatformModule = module {  }
