package off.kys.backtalk.data.repository

import android.os.Build
import android.os.ext.SdkExtensions
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.data.local.dao.MessagesDao
import off.kys.backtalk.domain.model.BackupData
import off.kys.backtalk.domain.repository.SyncRepository
import off.kys.backtalk.domain.use_case.SyncData
import off.kys.backtalk.sync.DeviceInfo
import off.kys.backtalk.sync.NsdHelper
import off.kys.backtalk.sync.SyncErrorCode
import off.kys.backtalk.sync.SyncException
import off.kys.backtalk.sync.SyncPacket
import off.kys.backtalk.sync.SyncSocketManager

class SyncRepositoryImpl(
    private val preferences: BacktalkPreferences,
    private val nsdHelper: NsdHelper,
    private val socketManager: SyncSocketManager,
    private val messagesDao: MessagesDao,
    private val syncData: SyncData
) : SyncRepository {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }

    private val _discoveredDevices = MutableStateFlow<List<DeviceInfo>>(emptyList())
    override val discoveredDevices = _discoveredDevices.asStateFlow()

    private val _pairedDevices = MutableStateFlow(loadPairedDevices())
    override val pairedDevices = _pairedDevices.asStateFlow()

    private var incomingRequestCallback: ((DeviceInfo) -> Unit)? = null
    private var pendingPairingPin: String? = null
    private var pendingPairingDevice: DeviceInfo? = null
    private var pairingResponseDeferred: CompletableDeferred<Boolean>? = null

    private fun loadPairedDevices(): List<DeviceInfo> {
        return try {
            json.decodeFromString<List<DeviceInfo>>(preferences.pairedDevicesJson)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun savePairedDevices(devices: List<DeviceInfo>) {
        preferences.pairedDevicesJson = json.encodeToString(devices)
        _pairedDevices.value = devices
    }

    override fun startDiscovery() {
        nsdHelper.discoverServices(
            onDeviceDiscovered = { serviceInfo ->
                val deviceId =
                    serviceInfo.attributes["deviceId"]?.let { String(it) }
                        ?: serviceInfo.serviceName

                val hostAddress =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && SdkExtensions.getExtensionVersion(
                            Build.VERSION_CODES.TIRAMISU
                        ) >= 7
                    ) {
                        serviceInfo.hostAddresses.firstOrNull()?.hostAddress
                    } else {
                        @Suppress("DEPRECATION")
                        serviceInfo.host.hostAddress
                    }


                val device = DeviceInfo(
                    id = deviceId,
                    name = serviceInfo.serviceName,
                    address = hostAddress,
                    port = serviceInfo.port,
                    lastSeenTimestamp = System.currentTimeMillis(),
                    isOnline = true
                )

                // Auto-update paired device info
                updateDeviceStatus(device, true)

                if (_discoveredDevices.value.none { it.id == device.id }) {
                    _discoveredDevices.value += device
                } else {
                    _discoveredDevices.value = _discoveredDevices.value.map {
                        if (it.id == device.id) device else it
                    }
                }
            },
            onDeviceLost = { serviceInfo ->
                val deviceName = serviceInfo.serviceName
                _discoveredDevices.value.find { it.name == deviceName }?.let { device ->
                    _discoveredDevices.value = _discoveredDevices.value.map {
                        if (it.id == device.id) it.copy(isOnline = false) else it
                    }
                    updateDeviceStatus(device, false)
                }
            }
        )
    }

    override fun stopDiscovery() {
        nsdHelper.stopDiscovery()
    }

    override fun startServer() {
        scope.launch {
            val port = socketManager.startServer { packet ->
                handleIncomingPacket(packet)
            }
            nsdHelper.registerService(port, Build.MODEL, preferences.deviceId)
        }
    }

    private suspend fun handleIncomingPacket(packet: SyncPacket): SyncPacket? = when (packet) {
        is SyncPacket.PairingRequest -> {
            val device = DeviceInfo(
                id = packet.deviceId,
                name = packet.deviceName,
                isOnline = true,
                lastSeenTimestamp = System.currentTimeMillis()
            )

            // Cancel any previous pending request
            pairingResponseDeferred?.cancel()

            pendingPairingDevice = device
            val deferred = CompletableDeferred<Boolean>()
            pairingResponseDeferred = deferred
            incomingRequestCallback?.invoke(device)

            // Wait for user to accept or refuse
            val accepted = withTimeoutOrNull(60000) {
                deferred.await()
            } ?: false

            SyncPacket.PairingResponse(accepted = accepted)
        }

        is SyncPacket.PinVerification -> {
            if (packet.pin == pendingPairingPin && pendingPairingDevice != null) {
                val pairedDevice = pendingPairingDevice!!.copy(
                    isPaired = true,
                    isOnline = true,
                    lastSeenTimestamp = System.currentTimeMillis()
                )
                val currentPaired = _pairedDevices.value
                savePairedDevices(currentPaired.filter { it.id != pairedDevice.id } + pairedDevice)
                pendingPairingPin = null
                pendingPairingDevice = null
                SyncPacket.PinVerificationResponse(success = true)
            } else {
                SyncPacket.PinVerificationResponse(success = false)
            }
        }

        is SyncPacket.DataUpdate -> {
            syncData(packet.backupData)
            SyncPacket.Ack(success = true)
        }

        is SyncPacket.SyncRequest -> {
            // Someone is requesting our data (Pull sync)
            updateDeviceStatusById(packet.requesterId)
            val messages = messagesDao.getAllMessages().first()
            val backupData = BackupData(messages = messages, preferences = emptyMap())
            SyncPacket.DataUpdate(backupData)
        }

        is SyncPacket.Disconnect -> {
            val updatedList = _pairedDevices.value.filter { it.id != packet.deviceId }
            savePairedDevices(updatedList)
            SyncPacket.Ack(success = true)
        }

        else -> null
    }

    override fun stopServer() {
        socketManager.stopServer()
        nsdHelper.unregisterService()
    }

    override suspend fun requestPairing(device: DeviceInfo): Result<Boolean> {
        val request = SyncPacket.PairingRequest(
            deviceName = Build.MODEL,
            deviceId = preferences.deviceId
        )
        val response = socketManager.sendPacketWithRetry(device.address!!, device.port, request)
        return when (response) {
            is SyncPacket.PairingResponse -> {
                if (response.accepted) {
                    updateDeviceStatus(device, true)
                    Result.success(true)
                } else {
                    Result.failure(SyncException(SyncErrorCode.PAIRING_REFUSED))
                }
            }
            is SyncPacket.Error -> {
                Result.failure(SyncException(response.errorCode, response.message))
            }
            else -> {
                Result.failure(SyncException(SyncErrorCode.CONNECTION_FAILED))
            }
        }
    }

    override suspend fun verifyPin(device: DeviceInfo, pin: String): Result<Boolean> {
        val request = SyncPacket.PinVerification(pin)
        val response = socketManager.sendPacketWithRetry(device.address!!, device.port, request)
        return when (response) {
            is SyncPacket.PinVerificationResponse -> {
                if (response.success) {
                    val pairedDevice = device.copy(
                        isPaired = true,
                        isOnline = true,
                        lastSeenTimestamp = System.currentTimeMillis()
                    )
                    val currentPaired = _pairedDevices.value
                    savePairedDevices(currentPaired.filter { it.id != pairedDevice.id } + pairedDevice)
                    Result.success(true)
                } else {
                    Result.failure(SyncException(SyncErrorCode.INVALID_PIN))
                }
            }
            is SyncPacket.Error -> {
                Result.failure(SyncException(response.errorCode, response.message))
            }
            else -> {
                Result.failure(SyncException(SyncErrorCode.CONNECTION_FAILED))
            }
        }
    }

    override suspend fun syncWithDevice(device: DeviceInfo): Result<Unit> {
        return try {
            val messages = messagesDao.getAllMessages().first()
            val backupData = BackupData(messages = messages, preferences = emptyMap())
            val packet = SyncPacket.DataUpdate(backupData)

            val response = socketManager.sendPacketWithRetry(device.address!!, device.port, packet)
            when (response) {
                is SyncPacket.Ack -> {
                    if (response.success) {
                        updateDeviceStatus(device, true)
                        Result.success(Unit)
                    } else {
                        updateDeviceStatus(device, false)
                        Result.failure(SyncException(SyncErrorCode.SYNC_FAILED))
                    }
                }
                is SyncPacket.Error -> {
                    updateDeviceStatus(device, false)
                    Result.failure(SyncException(response.errorCode, response.message))
                }
                else -> {
                    updateDeviceStatus(device, false)
                    Result.failure(SyncException(SyncErrorCode.CONNECTION_FAILED))
                }
            }
        } catch (e: Exception) {
            updateDeviceStatus(device, false)
            Result.failure(e)
        }
    }

    override suspend fun pullSync(device: DeviceInfo): Result<Unit> = try {
        val request = SyncPacket.SyncRequest(requesterId = preferences.deviceId)
        val response = socketManager.sendPacketWithRetry(device.address!!, device.port, request)
        when (response) {
            is SyncPacket.DataUpdate -> {
                syncData(response.backupData)
                updateDeviceStatus(device, true)
                Result.success(Unit)
            }
            is SyncPacket.Error -> {
                updateDeviceStatus(device, false)
                Result.failure(SyncException(response.errorCode, response.message))
            }
            else -> {
                updateDeviceStatus(device, false)
                Result.failure(SyncException(SyncErrorCode.CONNECTION_FAILED))
            }
        }
    } catch (e: Exception) {
        updateDeviceStatus(device, false)
        Result.failure(e)
    }

    private fun updateDeviceStatus(device: DeviceInfo, isOnline: Boolean) {
        val paired = _pairedDevices.value
        if (paired.any { it.id == device.id }) {
            val updated = paired.map {
                if (it.id == device.id) it.copy(
                    address = device.address ?: it.address,
                    port = if (device.port != 0) device.port else it.port,
                    isOnline = isOnline,
                    lastSeenTimestamp = if (isOnline) System.currentTimeMillis() else it.lastSeenTimestamp
                ) else it
            }
            savePairedDevices(updated)
        }
    }

    private fun updateDeviceStatusById(deviceId: String) {
        val paired = _pairedDevices.value
        if (paired.any { it.id == deviceId }) {
            val updated = paired.map {
                if (it.id == deviceId) it.copy(
                    isOnline = true,
                    lastSeenTimestamp = System.currentTimeMillis()
                ) else it
            }
            savePairedDevices(updated)
        }
    }

    override fun cleanupInvalidDevices() {
        val now = System.currentTimeMillis()
        val oneWeek = 7 * 24 * 60 * 60 * 1000L
        val updatedList = _pairedDevices.value.filter {
            // Keep if seen recently OR it's currently considered online
            it.isOnline || (now - it.lastSeenTimestamp < oneWeek)
        }
        if (updatedList.size != _pairedDevices.value.size) {
            savePairedDevices(updatedList)
        }
    }

    override fun disconnectDevice(device: DeviceInfo) {
        scope.launch {
            if (device.address != null) {
                socketManager.sendPacket(
                    device.address,
                    device.port,
                    SyncPacket.Disconnect(preferences.deviceId)
                )
            }
        }
        val updatedList = _pairedDevices.value.filter { it.id != device.id }
        savePairedDevices(updatedList)
    }

    override fun disconnectAll() {
        val devices = _pairedDevices.value
        scope.launch {
            devices.forEach { device ->
                if (device.address != null) {
                    socketManager.sendPacket(
                        device.address,
                        device.port,
                        SyncPacket.Disconnect(preferences.deviceId)
                    )
                }
            }
        }
        savePairedDevices(emptyList())
    }

    override fun onIncomingPairingRequest(callback: (DeviceInfo) -> Unit) {
        incomingRequestCallback = callback
    }

    override fun acceptPairingRequest(device: DeviceInfo, pin: String) {
        pendingPairingPin = pin
        pairingResponseDeferred?.complete(true)
    }

    override fun refusePairingRequest(device: DeviceInfo) {
        pairingResponseDeferred?.complete(false)
        pendingPairingDevice = null
        pendingPairingPin = null
    }

    override fun generatePin(): String = socketManager.generatePin()
}
