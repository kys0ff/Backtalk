package off.kys.backtalk.presentation.event

import android.net.Uri

sealed interface MediaPickerEvent {
    data class ToggleMediaSelection(val uri: Uri, val type: String) : MediaPickerEvent
    data class SelectFolder(val folderId: String) : MediaPickerEvent
    data class UpdateCaption(val text: String) : MediaPickerEvent
    data object RequestPermissions : MediaPickerEvent
    data class PermissionsResult(val cameraGranted: Boolean, val mediaGranted: Boolean) : MediaPickerEvent
    data object ClearSelection : MediaPickerEvent
}