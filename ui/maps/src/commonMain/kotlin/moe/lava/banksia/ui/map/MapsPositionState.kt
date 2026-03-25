package moe.lava.banksia.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import moe.lava.banksia.util.Point

class MapsPositionState internal constructor(
    private val scope: CoroutineScope
) {
    internal val updates: SharedFlow<Point>
        field = MutableSharedFlow()

    fun update(position: Point) {
        scope.launch { updates.emit(position) }
    }
}

@Composable
fun rememberMapsPositionState(): MapsPositionState {
    val scope = rememberCoroutineScope()
    return remember { MapsPositionState(scope) }
}
