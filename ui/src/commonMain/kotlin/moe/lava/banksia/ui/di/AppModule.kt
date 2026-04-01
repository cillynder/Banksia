package moe.lava.banksia.ui.di

import moe.lava.banksia.core.data.dataDiModule
import moe.lava.banksia.ui.screens.map.MapScreenViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val AppModule = module {
    includes(dataDiModule)

    // ViewModel
    viewModelOf(::MapScreenViewModel)
}
