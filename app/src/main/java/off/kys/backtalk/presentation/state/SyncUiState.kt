package off.kys.backtalk.presentation.state

import off.kys.backtalk.presentation.status.SyncStatus
import off.kys.backtalk.sync.DeviceInfo

data class SyncUiState(
    val discoveredDevices: List<DeviceInfo> = emptyList(),
    val pairedDevices: List<DeviceInfo> = emptyList(),
    val isDiscovering: Boolean = false,
    val incomingRequest: DeviceInfo? = null,
    val showPinDialog: Boolean = false,
    val pinToShow: String? = null,
    val syncStatus: SyncStatus = SyncStatus.IDLE,
    val error: String? = null,
    val errorRes: Int? = null,
    val deviceBeingPaired: DeviceInfo? = null,
    val deviceToUnpair: DeviceInfo? = null,
    val deviceToRePair: DeviceInfo? = null
)