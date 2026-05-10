package off.kys.backtalk.presentation.event

import off.kys.backtalk.sync.DeviceInfo

sealed interface SyncEvent {
    data object StartDiscovery : SyncEvent
    data object StopDiscovery : SyncEvent
    data class RequestPairing(val device: DeviceInfo) : SyncEvent
    data class AcceptPairingRequest(val device: DeviceInfo) : SyncEvent
    data class RefusePairingRequest(val device: DeviceInfo) : SyncEvent
    data class VerifyPin(val device: DeviceInfo, val pin: String) : SyncEvent
    data class SyncNow(val device: DeviceInfo) : SyncEvent
    data class PullSync(val device: DeviceInfo) : SyncEvent
    data object CleanupInvalidDevices : SyncEvent
    data class ConfirmUnpair(val device: DeviceInfo) : SyncEvent
    data class ConfirmRePair(val device: DeviceInfo) : SyncEvent
    data object DismissUnpairDialog : SyncEvent
    data object DismissRePairDialog : SyncEvent
    data class Disconnect(val device: DeviceInfo) : SyncEvent
    data object DismissIncomingRequest : SyncEvent
    data object DismissPinDialog : SyncEvent
    data object ClearError : SyncEvent
}
