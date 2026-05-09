package off.kys.backtalk.presentation.screen.sync.components

import androidx.compose.runtime.Composable
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
        UnpairDialog(device, viewModel)
    }

    state.deviceToRePair?.let { device ->
        RePairDialog(device, viewModel)
    }

    state.incomingRequest?.let { device ->
        IncomingRequestDialog(device, viewModel)
    }

    state.pinToShow?.let { pin ->
        PinDisplayDialog(pin, viewModel)
    }

    if (state.showPinDialog) {
        PinInputDialog(
            pinInput = pinInput,
            onPinInputChange = onPinInputChange,
            deviceBeingPaired = state.deviceBeingPaired,
            viewModel = viewModel
        )
    }

    if (state.syncStatus == SyncStatus.SYNCING) {
        SyncingProgressDialog()
    }

    state.error?.let { error ->
        ErrorDialog(error, viewModel)
    }
}