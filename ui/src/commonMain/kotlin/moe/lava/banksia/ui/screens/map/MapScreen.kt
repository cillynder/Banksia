package moe.lava.banksia.ui.screens.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.icerock.moko.geo.compose.BindLocationTrackerEffect
import dev.icerock.moko.geo.compose.LocationTrackerAccuracy
import dev.icerock.moko.geo.compose.rememberLocationTrackerFactory
import kotlinx.coroutines.launch
import moe.lava.banksia.resources.Res
import moe.lava.banksia.resources.my_location_24
import moe.lava.banksia.ui.layout.AppBottomSheet
import moe.lava.banksia.ui.layout.InfoPanel
import moe.lava.banksia.ui.layout.Searcher
import moe.lava.banksia.ui.layout.SheetStateWrapper
import moe.lava.banksia.ui.platform.BanksiaTheme
import moe.lava.banksia.ui.state.InfoPanelState
import moe.lava.banksia.util.Point
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

val MELBOURNE = Point(-37.8136, 144.9631)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun MapScreen(
    viewModel: MapScreenViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()

    val locationFactory = rememberLocationTrackerFactory(LocationTrackerAccuracy.Best)
    val locationTracker = remember { locationFactory.createLocationTracker() }
    BindLocationTrackerEffect(locationTracker)
    viewModel.bindTracker(locationTracker)
    scope.launch { locationTracker.startTracking() }

    val infoState by viewModel.infoState.collectAsStateWithLifecycle()
    val mapState by viewModel.mapState.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()

    val sheetState = SheetStateWrapper.create()
    var searchExpandedState by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(infoState) {
        if (infoState !is InfoPanelState.None) {
            sheetState.peek()
        } else {
            sheetState.hide()
        }
    }

    BanksiaTheme {
        Scaffold {
            Maps(
                modifier = Modifier.fillMaxSize(),
                state = mapState,
                onEvent = viewModel::handleEvent,
                cameraPositionFlow = viewModel.cameraChangeEmitter,
                extInsets = WindowInsets(top = with(LocalDensity.current) {
                    SearchBarDefaults.InputFieldHeight.roundToPx()
                }, bottom = sheetState.bottomInset),
                setLastKnownLocation = viewModel::setLastKnownLocation,
            )
            Searcher(
                state = searchState,
                onEvent = viewModel::handleEvent,
                expanded = searchExpandedState,
                onExpandedChange = {
                    searchExpandedState = it
                    if (it) scope.launch { sheetState.hide() }
                },
            )

            AnimatedVisibility(
                visible = !searchExpandedState,
                label = "search-hider",
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(
                            WindowInsets.safeContent.add(
                                WindowInsets(bottom = sheetState.bottomInset)
                            )
                        ),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    FloatingActionButton(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        onClick = { viewModel.centreCameraToLocation() },
                    ) {
                        Icon(painterResource(Res.drawable.my_location_24), "Move to current location")
                    }
                }

                AppBottomSheet(
                    sheetState = sheetState,
                    onDismiss = { viewModel.handleEvent(MapScreenEvent.DismissState) }
                ) {
                    InfoPanel(
                        state = infoState,
                        onEvent = viewModel::handleEvent,
                        onPeekHeightChange = { ph -> sheetState.peekTo(ph) },
                    )
                }
            }
        }
    }
}
