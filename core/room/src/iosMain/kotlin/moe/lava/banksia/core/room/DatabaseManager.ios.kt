package moe.lava.banksia.core.room

import org.koin.core.component.KoinComponent

actual class DatabaseManager actual constructor() : KoinComponent {
    actual val database: Database
        get() = TODO("Not yet implemented")
}
