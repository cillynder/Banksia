package moe.lava.banksia.core.sqld

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import org.koin.core.component.KoinComponent

actual class DatabaseManager : KoinComponent {
    actual val database by lazy {
        val driver = NativeSqliteDriver(BanksiaDatabase.Schema, "${DBNAME}.db")
        BanksiaDatabase(driver)
    }
}
