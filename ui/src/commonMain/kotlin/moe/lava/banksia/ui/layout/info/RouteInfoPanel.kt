package moe.lava.banksia.ui.layout.info

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import moe.lava.banksia.ui.components.RouteIcon
import moe.lava.banksia.ui.screens.map.MapScreenEvent
import moe.lava.banksia.ui.state.InfoPanelState

@Composable
internal fun RouteInfoPanel(
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
