package moe.lava.banksia.core.room

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.lava.banksia.core.util.error
import org.koin.core.component.KoinComponent
import java.io.File
import kotlin.system.exitProcess

actual class DatabaseManager : KoinComponent {
    private var liveDatabase: Database = Database.build(getBuilder())
    actual val database get() = liveDatabase

    private fun getBuilder(path: String = "./data/room.db"): RoomDatabase.Builder<Database> {
        val dbFile = File(path)
        return Room.databaseBuilder<Database>(
            name = dbFile.absolutePath,
        ).setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
    }

    fun makeAlt() = Database.build(getBuilder("./data/room_alt.db"))

    private fun deleteAll(file: File): Boolean {
        val r1 = file.takeIf { it.exists() }?.delete()
        val r2 = File(file.parentFile, file.name + ".lck").takeIf { it.exists() }?.delete()
        val r3 = File(file.parentFile, file.name + "-journal").takeIf { it.exists() }?.delete()
        return r1 != false && r2 != false && r3 != false
    }

    private fun renameAll(from: File, to: File): Boolean {
        val r1 = from.takeIf { it.exists() }?.renameTo(to)
        val r2 = File(from.parentFile, from.name + ".lck").takeIf { it.exists() }?.renameTo(File(to.parentFile, to.name + ".lck"))
        val r3 = File(from.parentFile, from.name + "-journal").takeIf { it.exists() }?.renameTo(File(to.parentFile, to.name + "-journal"))
        return r1 != false && r2 != false && r3 != false
    }

    fun swap(scope: CoroutineScope = CoroutineScope(Dispatchers.IO)) {
        val live = File("./data/room.db")
        val alt = File("./data/room_alt.db")
        val old = File("./data/room_old.db")

        if (!renameAll(live, old)) {
            error("DatabaseManager", "Failed to rename database from live to old (${live.absolutePath} -> ${old.absolutePath})")
            return
        }
        if (!renameAll(alt, live)) {
            error("DatabaseManager", "Failed to rename database from alt to live, trying to undo.. (${alt.absolutePath} -> ${live.absolutePath})")
            if (!live.renameTo(old)) {
                error("DatabaseManager", "Failed to undo, critical failure, exiting..")
                exitProcess(1)
            }
            return
        }
        val oldDatabase = liveDatabase
        liveDatabase = Database.build(getBuilder())

        scope.launch {
            delay(5000)
            if (!deleteAll(old)) {
                error("DatabaseManager", "Failed to unlink old database, stray files! (${old.absolutePath})")
            }
            oldDatabase.close()
        }
    }
}
