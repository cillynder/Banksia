package moe.lava.banksia.core.room

import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.core.parameter.ParametersHolder
import org.koin.core.scope.Scope
import java.io.File

class JvmDatabaseBuilder() : PlatformDatabaseBuilder {
    override fun getBuilder(): RoomDatabase.Builder<Database> {
        val dbFile = File("./data/room.db")
        return Room.databaseBuilder<Database>(
            name = dbFile.absolutePath,
        )
    }
}

internal actual fun Scope.provideDatabaseBuilder(p: ParametersHolder): PlatformDatabaseBuilder =
    JvmDatabaseBuilder()
