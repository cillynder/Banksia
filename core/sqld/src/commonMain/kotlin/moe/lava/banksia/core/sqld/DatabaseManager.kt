package moe.lava.banksia.core.sqld

import org.koin.core.component.KoinComponent

expect class DatabaseManager() : KoinComponent {
    val database: BanksiaDatabase
}
