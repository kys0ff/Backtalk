package off.kys.backtalk.presentation.screen.sync

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import off.kys.backtalk.R
import off.kys.backtalk.presentation.components.status_scaffold.ScaffoldStatus
import off.kys.backtalk.presentation.components.status_scaffold.StatusMessage
import off.kys.backtalk.presentation.components.status_scaffold.StatusScaffold
import off.kys.backtalk.presentation.event.SyncEvent
import off.kys.backtalk.presentation.screen.sync.components.SyncDeviceList
import off.kys.backtalk.presentation.screen.sync.components.SyncDialogs
import off.kys.backtalk.presentation.screen.sync.components.SyncTopBar
import off.kys.backtalk.presentation.status.SyncStatus
import off.kys.backtalk.presentation.viewmodel.SyncViewModel
import off.kys.backtalk.util.emptyString
import org.koin.compose.viewmodel.koinViewModel

class SyncScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: SyncViewModel = koinViewModel()
        val state by viewModel.state.collectAsState()
        var pinInput by remember { mutableStateOf(emptyString()) }

        LaunchedEffect(Unit) {
            viewModel.onEvent(SyncEvent.CleanupInvalidDevices)
            viewModel.onEvent(SyncEvent.StartDiscovery)
        }

        DisposableEffect(Unit) {
            onDispose { viewModel.onEvent(SyncEvent.StopDiscovery) }
        }

        val (scaffoldStatus, statusMessage) = remember(state.syncStatus, state.error, state.errorRes) {
            when {
                state.error != null || state.errorRes != null -> {
                    ScaffoldStatus.Error to (state.errorRes?.let { StatusMessage.Resource(it) }
                        ?: StatusMessage.Hardcoded(state.error ?: "Unknown error"))
                }

                state.syncStatus == SyncStatus.SYNCING -> ScaffoldStatus.Info to StatusMessage.Resource(
                    R.string.sync_status_syncing
                )

                state.syncStatus == SyncStatus.CONNECTING -> ScaffoldStatus.Info to StatusMessage.Resource(
                    R.string.common_please_wait
                )

                state.syncStatus == SyncStatus.PAIRING -> ScaffoldStatus.Info to StatusMessage.Resource(
                    R.string.sync_enter_pin
                )

                state.syncStatus == SyncStatus.COMPLETED -> ScaffoldStatus.Info to StatusMessage.Hardcoded(
                    "Sync completed"
                )

                else -> ScaffoldStatus.None to null
            }
        }

        StatusScaffold(
            status = scaffoldStatus,
            message = statusMessage,
            topBar = {
                SyncTopBar(
                    isDiscovering = state.isDiscovering,
                    onBackClick = { navigator.pop() },
                    onDiscoveryClick = {
                        if (state.isDiscovering) {
                            viewModel.onEvent(SyncEvent.StopDiscovery)
                        } else {
                            viewModel.onEvent(SyncEvent.StartDiscovery)
                        }
                    }
                )
            }
        ) { padding ->
            SyncDeviceList(
                padding = padding,
                state = state,
                onPairClick = { viewModel.onEvent(SyncEvent.RequestPairing(it)) },
                onPushClick = { viewModel.onEvent(SyncEvent.SyncNow(it)) },
                onPullClick = { viewModel.onEvent(SyncEvent.PullSync(it)) },
                onDisconnectClick = { viewModel.onEvent(SyncEvent.ConfirmUnpair(it)) }
            )

            SyncDialogs(
                state = state,
                viewModel = viewModel,
                pinInput = pinInput,
                onPinInputChange = { pinInput = it }
            )
        }
    }
}
