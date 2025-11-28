package moe.lava.banksia.ui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.PredictiveBackHandler
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composables.core.BottomSheet
import com.composables.core.BottomSheetState
import com.composables.core.DragIndication
import com.composables.core.SheetDetent
import com.composables.core.rememberBottomSheetState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AppBottomSheet(
    sheetState: SheetStateWrapper,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    var peekHeightMultiplier by remember { mutableFloatStateOf(1f) }
    var sheetEnabled by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    BottomSheet(
        state = sheetState.state,
        enabled = sheetEnabled,
        modifier = Modifier.fillMaxSize()
            // TODO: This recomposes; find a better solution using Modifier.layout
            .padding(
                top = 24.dp * (1f - peekHeightMultiplier),
                end = 24.dp * (1f - peekHeightMultiplier),
                bottom = 0.dp,
                start = 24.dp * (1f - peekHeightMultiplier),
            )
            .shadow(4.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .fillMaxWidth()
            .imePadding(),
    ) {
        Column(Modifier.fillMaxSize().alpha(peekHeightMultiplier)) {
            DragIndication(
                Modifier
                    .padding(vertical = 12.dp)
                    .height(4.dp)
                    .width(32.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(100))
            )
            content()
        }
    }

    PredictiveBackHandler(!sheetState.hidden) { progress ->
        sheetEnabled = false
        try {
            progress.collect { backEvent ->
                if (sheetState.peeking) {
                    peekHeightMultiplier = 1F - backEvent.progress
                }
            }
            if (sheetState.expanded) {
                scope.launch { sheetState.peek() }
            } else if (sheetState.peeking) {
                scope.launch {
                    sheetState.hide()
                    peekHeightMultiplier = 1F
                    onDismiss()
                }
            }
        } catch (_: CancellationException) {
            peekHeightMultiplier = 1F
        }
        sheetEnabled = true
    }
}

class SheetStateWrapper(
    val state: BottomSheetState,
    private val scope: CoroutineScope,
    private var p1: MutableState<Dp>,
    private var p2: MutableState<Dp>,
    private val peek1: SheetDetent,
    private val peek2: SheetDetent,
) {
    companion object {
        private val saver = Saver<MutableState<Dp>, Float>(
            save = { it.value.value },
            restore = { mutableStateOf(it.dp) }
        )

        @Composable
        fun create(): SheetStateWrapper {
            val p1 = rememberSaveable(saver = saver) { mutableStateOf(0.dp) }
            val p2 = rememberSaveable(saver = saver) { mutableStateOf(0.dp) }
            val scope = rememberCoroutineScope()

            val peek1 = SheetDetent(identifier = "peek1") { containerHeight, sheetHeight ->
                val res = (p1.value + 40.dp)
                res
            }
            val peek2 = SheetDetent(identifier = "peek2") { containerHeight, sheetHeight ->
                val res = (p2.value + 40.dp)
                res
            }
            val internalState = rememberBottomSheetState(
                initialDetent = SheetDetent.Hidden,
                detents = listOf(SheetDetent.Hidden, peek1, peek2, SheetDetent.FullyExpanded)
            )
            return remember { SheetStateWrapper(internalState, scope, p1, p2, peek1, peek2) }
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun stateEither(detent: SheetDetent) = state.currentDetent == detent || state.targetDetent == detent

    private var peek: SheetDetent = peek1

    val current get() = state.currentDetent
    val target get() = state.targetDetent
    val expanded get() = stateEither(SheetDetent.FullyExpanded)
    val peeking get() = stateEither(peek1) || stateEither(peek2)
    val hidden get() = stateEither(SheetDetent.Hidden)
    val offset get() = state.offset

    val bottomInset: Int @Composable get() {
        return if (!hidden) {
            val sheetOffset = state.offset.roundToInt()
            val insets = WindowInsets.safeDrawing.getBottom(LocalDensity.current)
            (sheetOffset - insets)
                .coerceAtLeast(0)
                .coerceIn(0, with(LocalDensity.current) { 500.dp.roundToPx() })
        } else 0
    }

    fun hide() { state.targetDetent = SheetDetent.Hidden }
    fun peek() { state.targetDetent = peek }
    fun peekTo(target: Dp) {
        if (peek == peek1) {
            p2.value = target
            peek = peek2
        } else {
            p1.value = target
            peek = peek1
        }
        state.invalidateDetents()
        state.targetDetent = peek
        // TODO: this is broken; animateTo never finishes
        // scope.launch {
        //     state.animateTo(peek)
        //     p1.value = target
        //     p2.value = target
        //     state.invalidateDetents()
        // }
    }
}
