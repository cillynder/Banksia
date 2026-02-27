package moe.lava.banksia.ui.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import moe.lava.banksia.ui.components.RouteIcon
import moe.lava.banksia.ui.screens.map.MapScreenEvent
import moe.lava.banksia.ui.state.InfoPanelState
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun InfoPanel(
    state: InfoPanelState,
    onEvent: (MapScreenEvent) -> Unit,
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
                is InfoPanelState.Route -> RouteInfoPanel(state, onEvent)
                is InfoPanelState.Stop -> StopInfoPanel(state, onEvent)
                is InfoPanelState.Run -> RunInfoPanel(state, onEvent)
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

@Composable
private inline fun RouteInfoPanel(
    state: InfoPanelState.Route,
    onEvent: (MapScreenEvent) -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Row {
            RouteIcon(routeType = state.type)
            Text(
                state.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
private inline fun RunInfoPanel(
    state: InfoPanelState.Run,
    onEvent: (MapScreenEvent) -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Row {
            RouteIcon(routeType = state.type)
            Text(
                "${state.direction} via ${state.routeName ?: "..."}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
private inline fun StopInfoPanel(
    state: InfoPanelState.Stop,
    onEvent: (MapScreenEvent) -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            state.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Start
        )
        state.subname?.let {
            Text(
                "/ $it",
                modifier = Modifier.padding(start = 5.dp),
                style = MaterialTheme.typography.titleSmall,
                color = Color.Gray,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Start
            )
        }
        state.departures?.let {
            Spacer(Modifier.height(5.dp))
            it.forEach { (name, formatted) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        formatted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 5.dp)
                    )
                }
            }
        }
    }
}
