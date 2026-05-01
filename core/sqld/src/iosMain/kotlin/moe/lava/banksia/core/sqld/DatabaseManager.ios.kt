package moe.lava.banksia.core.sqld

import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual class DatabaseManager actual constructor() : org.koin.core.component.KoinComponent {
    actual val database by lazy {
        val driver = NativeSqliteDriver(BanksiaDatabase.Schema, "timetable.db")
        BanksiaDatabase(driver)
    }
}
