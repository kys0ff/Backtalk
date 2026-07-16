package off.kys.backtalk.presentation.state.preferences

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import off.kys.backtalk.presentation.status.SyncStatus
import off.kys.backtalk.sync.DeviceInfo

data class SyncUiState(
    val discoveredDevices: PersistentList<DeviceInfo> = persistentListOf(),
    val pairedDevices: PersistentList<DeviceInfo> = persistentListOf(),
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