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
import moe.lava.banksia.core.model.RouteType
import moe.lava.banksia.ui.components.RouteIcon

sealed class RouteInfoPanelEvent : InfoPanelEvent()

data class RouteInfoPanelState(
    val name: String,
    val type: RouteType,
) : InfoPanelState() {
    override val loading = false
}

@Composable
internal fun RouteInfoPanel(
    state: RouteInfoPanelState,
    onEvent: (RouteInfoPanelEvent) -> Unit,
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
