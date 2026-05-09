package off.kys.backtalk.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import off.kys.backtalk.domain.repository.SyncRepository
import off.kys.backtalk.presentation.state.SyncUiState
import off.kys.backtalk.presentation.status.SyncStatus
import off.kys.backtalk.sync.DeviceInfo

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

    fun startDiscovery() {
        _state.value = _state.value.copy(isDiscovering = true)
        syncRepository.startDiscovery()
        syncRepository.startServer()
    }

    fun stopDiscovery() {
        _state.value = _state.value.copy(isDiscovering = false)
        syncRepository.stopDiscovery()
    }

    fun requestPairing(device: DeviceInfo) {
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
                _state.value = _state.value.copy(
                    syncStatus = SyncStatus.FAILED,
                    error = result.exceptionOrNull()?.message ?: "Pairing request failed",
                    deviceBeingPaired = null
                )
            }
        }
    }

    fun acceptRequest(device: DeviceInfo) {
        stopDiscovery()
        val pin = syncRepository.generatePin()
        _state.value = _state.value.copy(
            incomingRequest = null,
            pinToShow = pin,
            syncStatus = SyncStatus.PAIRING,
            deviceBeingPaired = device
        )
        syncRepository.acceptPairingRequest(device, pin)
    }

    fun refuseRequest(device: DeviceInfo) {
        _state.value = _state.value.copy(incomingRequest = null)
        syncRepository.refusePairingRequest(device)
    }

    fun verifyPin(device: DeviceInfo, pin: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(syncStatus = SyncStatus.PAIRING)
            val result = syncRepository.verifyPin(device, pin)
            if (result.isSuccess) {
                _state.value = _state.value.copy(
                    showPinDialog = false,
                    syncStatus = SyncStatus.COMPLETED
                )
            } else {
                _state.value = _state.value.copy(
                    syncStatus = SyncStatus.FAILED,
                    error = result.exceptionOrNull()?.message ?: "PIN verification failed"
                )
            }
        }
    }

    fun syncNow(device: DeviceInfo) {
        viewModelScope.launch {
            _state.value = _state.value.copy(syncStatus = SyncStatus.SYNCING)
            val result = syncRepository.syncWithDevice(device)
            if (result.isSuccess) {
                _state.value = _state.value.copy(syncStatus = SyncStatus.COMPLETED)
            } else {
                _state.value = _state.value.copy(
                    syncStatus = SyncStatus.FAILED,
                    error = result.exceptionOrNull()?.message ?: "Sync failed"
                )
            }
        }
    }

    fun pullSync(device: DeviceInfo) {
        viewModelScope.launch {
            _state.value = _state.value.copy(syncStatus = SyncStatus.SYNCING)
            val result = syncRepository.pullSync(device)
            if (result.isSuccess) {
                _state.value = _state.value.copy(syncStatus = SyncStatus.COMPLETED)
            } else {
                _state.value = _state.value.copy(
                    syncStatus = SyncStatus.FAILED,
                    error = result.exceptionOrNull()?.message ?: "Pull sync failed"
                )
            }
        }
    }

    fun cleanupInvalidDevices() {
        syncRepository.cleanupInvalidDevices()
    }

    fun confirmUnpair(device: DeviceInfo) {
        _state.value = _state.value.copy(deviceToUnpair = device)
    }

    fun confirmRePair(device: DeviceInfo) {
        syncRepository.disconnectDevice(device)
        _state.value = _state.value.copy(deviceToRePair = null)
        performPairing(device)
    }

    fun dismissUnpairDialog() {
        _state.value = _state.value.copy(deviceToUnpair = null)
    }

    fun dismissRePairDialog() {
        _state.value = _state.value.copy(deviceToRePair = null)
    }

    fun disconnect(device: DeviceInfo) {
        syncRepository.disconnectDevice(device)
        _state.value = _state.value.copy(deviceToUnpair = null)
    }

    fun dismissIncomingRequest() {
        _state.value = _state.value.copy(incomingRequest = null)
    }

    fun dismissPinDialog() {
        _state.value = _state.value.copy(
            showPinDialog = false,
            pinToShow = null,
            deviceBeingPaired = null
        )
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null, syncStatus = SyncStatus.IDLE)
    }

    override fun onCleared() {
        super.onCleared()
        syncRepository.stopDiscovery()
        syncRepository.stopServer()
    }
}
