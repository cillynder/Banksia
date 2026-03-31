package moe.lava.banksia.ui.layout.info

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

sealed class StopInfoPanelEvent : InfoPanelEvent()

data class StopInfoPanelState(
    val id: String,
    val name: String,
    val subname: String? = null,
    val departures: List<Departure>? = null,
) : InfoPanelState() {
    override val loading: Boolean
        get() = departures == null

    data class Departure(val directionName: String, val formattedTimes: String)
}

@Composable
internal fun StopInfoPanel(
    state: StopInfoPanelState,
    onEvent: (StopInfoPanelEvent) -> Unit,
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
