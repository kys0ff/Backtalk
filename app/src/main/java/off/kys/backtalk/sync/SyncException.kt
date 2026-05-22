package off.kys.backtalk.sync

import off.kys.backtalk.R

class SyncException(
    val errorCode: SyncErrorCode,
    message: String? = null,
    cause: Throwable? = null
) : Exception(message ?: errorCode.name, cause) {
    fun getErrorMessageRes(): Int = when (errorCode) {
        SyncErrorCode.UNKNOWN -> R.string.sync_error_unknown
        SyncErrorCode.CONNECTION_FAILED -> R.string.sync_error_connection_failed
        SyncErrorCode.TIMEOUT -> R.string.sync_error_timeout
        SyncErrorCode.PAIRING_REFUSED -> R.string.sync_error_pairing_refused
        SyncErrorCode.INVALID_PIN -> R.string.sync_error_invalid_pin
        SyncErrorCode.DEVICE_NOT_FOUND -> R.string.sync_error_device_not_found
        SyncErrorCode.SYNC_FAILED -> R.string.sync_error_sync_failed
        SyncErrorCode.UNAUTHORIZED -> R.string.sync_error_unauthorized
        SyncErrorCode.PROTOCOL_ERROR -> R.string.sync_error_protocol_error
    }
}
