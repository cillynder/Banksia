package moe.lava.banksia.ui.platform

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BanksiaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable (() -> Unit)
) {
    MaterialExpressiveTheme(
        colorScheme = BanksiaTheme.colors(darkTheme, dynamicColor),
        content = content,
    )
}

@Composable
expect fun BanksiaTheme.colors(darkTheme: Boolean, dynamicColor: Boolean): ColorScheme

object BanksiaTheme {
    val colors: ColorScheme
        @Composable
        get() = colors(isSystemInDarkTheme(), true)
}
