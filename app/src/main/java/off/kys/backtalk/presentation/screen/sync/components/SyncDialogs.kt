package off.kys.backtalk.presentation.screen.sync.components

import androidx.compose.runtime.Composable
import off.kys.backtalk.presentation.event.SyncEvent
import off.kys.backtalk.presentation.state.SyncUiState
import off.kys.backtalk.presentation.status.SyncStatus
import off.kys.backtalk.presentation.viewmodel.SyncViewModel

@Composable
fun SyncDialogs(
    state: SyncUiState,
    viewModel: SyncViewModel,
    pinInput: String,
    onPinInputChange: (String) -> Unit
) {
    state.deviceToUnpair?.let { device ->
        UnpairDialog(
            device = device,
            onDisconnectDevice = { viewModel.onEvent(SyncEvent.Disconnect(it)) }
        ) {
            viewModel.onEvent(
                SyncEvent.DismissUnpairDialog
            )
        }
    }

    state.deviceToRePair?.let { device ->
        RePairDialog(
            device = device,
            onConfirmRePair = { viewModel.onEvent(SyncEvent.ConfirmRePair(device)) },
            onDismissRequest = { viewModel.onEvent(SyncEvent.DismissRePairDialog) }
        )
    }

    state.incomingRequest?.let { device ->
        IncomingRequestDialog(
            device = device,
            onAcceptPairingRequest = { viewModel.onEvent(SyncEvent.AcceptPairingRequest(device)) },
            onRefusePairingRequest = { viewModel.onEvent(SyncEvent.RefusePairingRequest(device)) },
            onDismissRequest = { viewModel.onEvent(SyncEvent.DismissIncomingRequest) }
        )
    }

    state.pinToShow?.let { pin ->
        PinDisplayDialog(pin) { viewModel.onEvent(SyncEvent.DismissPinDialog) }
    }

    if (state.showPinDialog) {
        PinInputDialog(
            pinInput = pinInput,
            onPinInputChange = onPinInputChange,
            deviceBeingPaired = state.deviceBeingPaired,
            onVerifyPin = { device, pin ->
                device?.let {
                    viewModel.onEvent(SyncEvent.VerifyPin(it, pin))
                }
            },
            onDismissRequest = { viewModel.onEvent(SyncEvent.DismissPinDialog) }
        )
    }

    if (state.syncStatus == SyncStatus.SYNCING) {
        SyncingProgressDialog()
    }

    if (state.error != null || state.errorRes != null) {
        ErrorDialog(
            error = state.error,
            errorRes = state.errorRes
        ) {
            viewModel.onEvent(SyncEvent.ClearError)
        }
    }
}
