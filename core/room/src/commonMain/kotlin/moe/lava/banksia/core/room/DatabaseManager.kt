package moe.lava.banksia.core.room

import org.koin.core.component.KoinComponent

expect class DatabaseManager() : KoinComponent {
    val database: Database
}
