package moe.lava.banksia.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import moe.lava.banksia.ui.map.util.CameraPosition

class MapsPositionState internal constructor(
    private val scope: CoroutineScope
) {
    internal val updates: SharedFlow<CameraPosition>
        field = MutableSharedFlow()

    fun update(position: CameraPosition) {
        scope.launch {
            updates.emit(position)
        }
    }
}

@Composable
fun rememberMapsPositionState(): MapsPositionState {
    val scope = rememberCoroutineScope()
    return remember { MapsPositionState(scope) }
}
