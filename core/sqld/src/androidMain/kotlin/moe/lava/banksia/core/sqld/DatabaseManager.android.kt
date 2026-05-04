package moe.lava.banksia.core.sqld

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

actual class DatabaseManager : KoinComponent {
    actual val database by lazy {
        val ctx = get<Context>().applicationContext
        val driver = AndroidSqliteDriver(BanksiaDatabase.Schema, ctx, "${DBNAME}.db")
        BanksiaDatabase(driver)
    }
}
