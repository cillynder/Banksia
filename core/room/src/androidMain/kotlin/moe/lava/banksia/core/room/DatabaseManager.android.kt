package moe.lava.banksia.core.room

import android.content.Context
import androidx.room.Room
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

actual class DatabaseManager actual constructor() : KoinComponent {
    private val ctx by inject<Context>()

    actual val database by lazy {
        val ctx = get<Context>().applicationContext
        val dbFile = ctx.getDatabasePath("room.db")
        val builder = Room.databaseBuilder<Database>(
            context = ctx,
            name = dbFile.absolutePath,
        )

        Database.build(builder)
    }
}
