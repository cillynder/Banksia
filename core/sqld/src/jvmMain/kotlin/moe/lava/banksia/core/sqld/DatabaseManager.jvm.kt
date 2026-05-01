package moe.lava.banksia.core.sqld

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.lava.banksia.core.util.error
import org.koin.core.component.KoinComponent
import java.io.File
import java.util.Properties
import kotlin.system.exitProcess

private const val DBNAME = "timetable"

actual class DatabaseManager actual constructor() : KoinComponent {
    private var driver = connect()
    actual val database get() = BanksiaDatabase(driver)

    private fun connect(path: String = "./data/${DBNAME}.db") =
        JdbcSqliteDriver("jdbc:sqlite:${path}", Properties(), BanksiaDatabase.Schema)
            .apply { execute(null, "PRAGMA journal_mode = OFF;", 0) }

    fun makeAlt() = run {
        File("./data/${DBNAME}_alt.db").takeIf { it.exists() }?.delete()
        val driver = connect("./data/${DBNAME}_alt.db")
        BanksiaDatabase(driver) to { driver.close() }
    }

    fun swap(scope: CoroutineScope = CoroutineScope(Dispatchers.IO)) {
        val live = File("./data/${DBNAME}.db")
        val alt = File("./data/${DBNAME}_alt.db")
        val old = File("./data/${DBNAME}_old.db")

        if (live.takeIf { it.exists() }?.renameTo(old) == false) {
            error("DatabaseManager", "Failed to rename database from live to old (${live.absolutePath} -> ${old.absolutePath})")
            return
        }
        if (alt.takeIf { it.exists() }?.renameTo(live) == false) {
            error("DatabaseManager", "Failed to rename database from alt to live, trying to undo.. (${alt.absolutePath} -> ${live.absolutePath})")
            if (!live.renameTo(old)) {
                error("DatabaseManager", "Failed to undo, critical failure, exiting..")
                exitProcess(1)
            }
            return
        }
        val oldDriver = driver
        driver = connect()

        scope.launch {
            delay(5000)
            if (old.takeIf { it.exists() }?.delete() == false) {
                error("DatabaseManager", "Failed to unlink old database, stray files! (${old.absolutePath})")
            }
            oldDriver.close()
        }
    }
}
