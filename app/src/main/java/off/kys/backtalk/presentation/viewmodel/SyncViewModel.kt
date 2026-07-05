package off.kys.backtalk.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import off.kys.backtalk.domain.repository.SyncRepository
import off.kys.backtalk.R
import off.kys.backtalk.presentation.event.SyncEvent
import off.kys.backtalk.presentation.state.preferences.SyncUiState
import off.kys.backtalk.presentation.status.SyncStatus
import off.kys.backtalk.sync.DeviceInfo
import off.kys.backtalk.sync.SyncException

class SyncViewModel(
    private val syncRepository: SyncRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SyncUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            syncRepository.discoveredDevices.collect { devices ->
                _state.value = _state.value.copy(discoveredDevices = devices)
            }
        }
        viewModelScope.launch {
            syncRepository.pairedDevices.collect { devices ->
                val currentState = _state.value
                val deviceIdToMatch = currentState.deviceBeingPaired?.id
                
                val isPairedNow = deviceIdToMatch != null && devices.any { it.id == deviceIdToMatch && it.isPaired }
                
                if (isPairedNow && (currentState.pinToShow != null || currentState.showPinDialog)) {
                    _state.value = currentState.copy(
                        pairedDevices = devices,
                        pinToShow = null,
                        showPinDialog = false,
                        deviceBeingPaired = null,
                        syncStatus = SyncStatus.COMPLETED
                    )
                } else {
                    _state.value = currentState.copy(pairedDevices = devices)
                }
            }
        }
        syncRepository.onIncomingPairingRequest { device ->
            _state.value = _state.value.copy(incomingRequest = device)
        }
    }

    fun onEvent(event: SyncEvent) {
        when (event) {
            SyncEvent.StartDiscovery -> startDiscovery()
            SyncEvent.StopDiscovery -> stopDiscovery()
            is SyncEvent.RequestPairing -> requestPairing(event.device)
            is SyncEvent.AcceptPairingRequest -> acceptRequest(event.device)
            is SyncEvent.RefusePairingRequest -> refuseRequest()
            is SyncEvent.VerifyPin -> verifyPin(event.device, event.pin)
            is SyncEvent.SyncNow -> syncNow(event.device)
            is SyncEvent.PullSync -> pullSync(event.device)
            SyncEvent.CleanupInvalidDevices -> cleanupInvalidDevices()
            is SyncEvent.ConfirmUnpair -> confirmUnpair(event.device)
            is SyncEvent.ConfirmRePair -> confirmRePair(event.device)
            SyncEvent.DismissUnpairDialog -> dismissUnpairDialog()
            SyncEvent.DismissRePairDialog -> dismissRePairDialog()
            is SyncEvent.Disconnect -> disconnect(event.device)
            SyncEvent.DismissIncomingRequest -> dismissIncomingRequest()
            SyncEvent.DismissPinDialog -> dismissPinDialog()
            SyncEvent.ClearError -> clearError()
        }
    }

    private fun startDiscovery() {
        _state.value = _state.value.copy(isDiscovering = true)
        syncRepository.startDiscovery()
        syncRepository.startServer()
    }

    private fun stopDiscovery() {
        _state.value = _state.value.copy(isDiscovering = false)
        syncRepository.stopDiscovery()
    }

    private fun requestPairing(device: DeviceInfo) {
        val isAlreadyPaired = _state.value.pairedDevices.any { it.id == device.id }
        if (isAlreadyPaired) {
            _state.value = _state.value.copy(deviceToRePair = device)
            return
        }
        performPairing(device)
    }

    private fun performPairing(device: DeviceInfo) {
        stopDiscovery()
        viewModelScope.launch {
            _state.value = _state.value.copy(
                syncStatus = SyncStatus.CONNECTING,
                deviceBeingPaired = device
            )
            val result = syncRepository.requestPairing(device)
            if (result.isSuccess) {
                _state.value = _state.value.copy(
                    syncStatus = SyncStatus.PAIRING,
                    showPinDialog = true
                )
            } else {
                val exception = result.exceptionOrNull()
                _state.value = _state.value.copy(
                    syncStatus = SyncStatus.FAILED,
                    error = if (exception is SyncException) null else exception?.message,
                    errorRes = (exception as? SyncException)?.getErrorMessageRes() ?: R.string.error_pairing_failed,
                    deviceBeingPaired = null
                )
            }
        }
    }

    private fun acceptRequest(device: DeviceInfo) {
        stopDiscovery()
        val pin = syncRepository.generatePin()
        _state.value = _state.value.copy(
            incomingRequest = null,
            pinToShow = pin,
            syncStatus = SyncStatus.PAIRING,
            deviceBeingPaired = device
        )
        syncRepository.acceptPairingRequest(pin)
    }

    private fun refuseRequest() {
        _state.value = _state.value.copy(incomingRequest = null)
        syncRepository.refusePairingRequest()
    }

    private fun verifyPin(device: DeviceInfo, pin: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(syncStatus = SyncStatus.PAIRING)
            val result = syncRepository.verifyPin(device, pin)
            if (result.isSuccess) {
                _state.value = _state.value.copy(
                    showPinDialog = false,
                    syncStatus = SyncStatus.COMPLETED
                )
            } else {
                val exception = result.exceptionOrNull()
                _state.value = _state.value.copy(
                    syncStatus = SyncStatus.FAILED,
                    error = if (exception is SyncException) null else exception?.message,
                    errorRes = (exception as? SyncException)?.getErrorMessageRes() ?: R.string.error_pin_verification_failed
                )
            }
        }
    }

    private fun syncNow(device: DeviceInfo) {
        viewModelScope.launch {
            _state.value = _state.value.copy(syncStatus = SyncStatus.SYNCING)
            val result = syncRepository.syncWithDevice(device)
            if (result.isSuccess) {
                _state.value = _state.value.copy(syncStatus = SyncStatus.COMPLETED)
            } else {
                val exception = result.exceptionOrNull()
                _state.value = _state.value.copy(
                    syncStatus = SyncStatus.FAILED,
                    error = if (exception is SyncException) null else exception?.message,
                    errorRes = (exception as? SyncException)?.getErrorMessageRes() ?: R.string.error_sync_failed
                )
            }
        }
    }

    private fun pullSync(device: DeviceInfo) {
        viewModelScope.launch {
            _state.value = _state.value.copy(syncStatus = SyncStatus.SYNCING)
            val result = syncRepository.pullSync(device)
            if (result.isSuccess) {
                _state.value = _state.value.copy(syncStatus = SyncStatus.COMPLETED)
            } else {
                val exception = result.exceptionOrNull()
                _state.value = _state.value.copy(
                    syncStatus = SyncStatus.FAILED,
                    error = if (exception is SyncException) null else exception?.message,
                    errorRes = (exception as? SyncException)?.getErrorMessageRes() ?: R.string.error_pull_sync_failed
                )
            }
        }
    }

    private fun cleanupInvalidDevices() {
        syncRepository.cleanupInvalidDevices()
    }

    private fun confirmUnpair(device: DeviceInfo) {
        _state.value = _state.value.copy(deviceToUnpair = device)
    }

    private fun confirmRePair(device: DeviceInfo) {
        syncRepository.disconnectDevice(device)
        _state.value = _state.value.copy(deviceToRePair = null)
        performPairing(device)
    }

    private fun dismissUnpairDialog() {
        _state.value = _state.value.copy(deviceToUnpair = null)
    }

    private fun dismissRePairDialog() {
        _state.value = _state.value.copy(deviceToRePair = null)
    }

    private fun disconnect(device: DeviceInfo) {
        syncRepository.disconnectDevice(device)
        _state.value = _state.value.copy(deviceToUnpair = null)
    }

    private fun dismissIncomingRequest() {
        _state.value = _state.value.copy(incomingRequest = null)
    }

    private fun dismissPinDialog() {
        _state.value = _state.value.copy(
            showPinDialog = false,
            pinToShow = null,
            deviceBeingPaired = null
        )
    }

    private fun clearError() {
        _state.value = _state.value.copy(
            error = null,
            errorRes = null,
            syncStatus = SyncStatus.IDLE
        )
    }

    override fun onCleared() {
        super.onCleared()
        syncRepository.stopDiscovery()
        syncRepository.stopServer()
    }
}
