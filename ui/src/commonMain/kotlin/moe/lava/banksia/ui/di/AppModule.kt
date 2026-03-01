package moe.lava.banksia.ui.di

import moe.lava.banksia.client.di.ClientModule
import moe.lava.banksia.ui.screens.map.MapScreenViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val AppModule = module {
    includes(ClientModule)
    // ViewModel
    viewModelOf(::MapScreenViewModel)
}
