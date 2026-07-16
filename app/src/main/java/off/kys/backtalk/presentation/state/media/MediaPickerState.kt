package off.kys.backtalk.presentation.state.media

import android.net.Uri
import off.kys.backtalk.domain.model.MediaFolder
import off.kys.backtalk.domain.model.MediaItem
import off.kys.backtalk.util.emptyString

data class MediaPickerState(
    val mediaItems: List<MediaItem> = emptyList(),
    val folders: List<MediaFolder> = emptyList(),
    val filteredMediaItems: List<MediaItem> = emptyList(),
    val selectedFolderId: String = "all",
    val selectedUris: Set<Uri> = emptySet(),
    val selectedType: String = "image/jpeg",
    val captionText: String = emptyString(),
    val hasCameraPermission: Boolean = false,
    val hasMediaPermission: Boolean = false,
    val isLoading: Boolean = false
)