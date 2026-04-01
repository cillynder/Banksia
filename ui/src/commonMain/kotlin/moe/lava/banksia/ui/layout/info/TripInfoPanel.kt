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

sealed class TripInfoPanelEvent : InfoPanelEvent()

data class TripInfoPanelState(
    val direction: String,
    val type: RouteType,
    val routeName: String? = null,
) : InfoPanelState() {
    override val loading = routeName == null
}

@Composable
internal fun TripInfoPanel(
    state: TripInfoPanelState,
    onEvent: (TripInfoPanelEvent) -> Unit,
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
