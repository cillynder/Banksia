package moe.lava.banksia.ui.platform

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
actual fun BanksiaTheme.colors(
    darkTheme: Boolean,
    dynamicColor: Boolean
): ColorScheme = when {
    darkTheme -> darkColorScheme()
    else -> lightColorScheme()
}
