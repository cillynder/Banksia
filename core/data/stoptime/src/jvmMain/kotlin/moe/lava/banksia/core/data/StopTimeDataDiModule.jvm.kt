package moe.lava.banksia.core.data

import moe.lava.banksia.core.data.repositories.StopTimeRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal actual val platformModule = module {
    singleOf(::StopTimeRepository)
}
