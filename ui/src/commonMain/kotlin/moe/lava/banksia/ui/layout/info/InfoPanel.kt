package moe.lava.banksia.ui.layout.info

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

sealed class InfoPanelEvent

sealed class InfoPanelState {
    abstract val loading: Boolean

    data object None : InfoPanelState() {
        override val loading = false
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun InfoPanel(
    state: InfoPanelState,
    onEvent: (InfoPanelEvent) -> Unit,
    onPeekHeightChange: (Dp) -> Unit,
) {
    if (state is InfoPanelState.None)
        return

    val localDensity = LocalDensity.current
    var delayedLoad by remember { mutableStateOf(false) }

    LaunchedEffect(state.loading) {
        if (state.loading) {
            delay(200.milliseconds)
            delayedLoad = true
        } else {
            delayedLoad = false
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .onSizeChanged {
                onPeekHeightChange(with(localDensity) { it.height.toDp().coerceAtMost(250.dp) })
            }
    ) {
        Box {
            when (state) {
                is RouteInfoPanelState -> RouteInfoPanel(state, onEvent)
                is StopInfoPanelState -> StopInfoPanel(state, onEvent)
                is TripInfoPanelState -> TripInfoPanel(state, onEvent)
                is InfoPanelState.None -> throw UnsupportedOperationException()
            }

            this@Column.AnimatedVisibility(
                modifier = Modifier.align(Alignment.TopEnd),
                visible = delayedLoad,
                label = "sheet-loading",
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
            ) {
                LoadingIndicator(
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeContent))
    }
}
