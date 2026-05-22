package off.kys.backtalk.domain.repository

import kotlinx.coroutines.flow.Flow
import off.kys.backtalk.sync.DeviceInfo

interface SyncRepository {
    val discoveredDevices: Flow<List<DeviceInfo>>
    val pairedDevices: Flow<List<DeviceInfo>>
    
    fun startDiscovery()
    fun stopDiscovery()
    fun startServer()
    fun stopServer()
    
    suspend fun requestPairing(device: DeviceInfo): Result<Boolean>
    suspend fun verifyPin(device: DeviceInfo, pin: String): Result<Boolean>
    suspend fun syncWithDevice(device: DeviceInfo): Result<Unit>
    suspend fun pullSync(device: DeviceInfo): Result<Unit>
    fun disconnectDevice(device: DeviceInfo)
    fun disconnectAll()
    fun cleanupInvalidDevices()

    fun onIncomingPairingRequest(callback: (DeviceInfo) -> Unit)
    fun acceptPairingRequest(pin: String)
    fun refusePairingRequest()
    fun generatePin(): String
}
