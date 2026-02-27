package moe.lava.banksia.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import moe.lava.banksia.client.di.ClientModule
import moe.lava.banksia.di.CommonModules
import moe.lava.banksia.ui.screens.map.MapScreen
import org.koin.compose.KoinMultiplatformApplication
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.koinConfiguration

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class, KoinExperimentalAPI::class)
@Composable
fun App() {
    KoinMultiplatformApplication(config = koinConfiguration {
        modules(CommonModules, ClientModule)
    }) {
        MapScreen()
    }
}
