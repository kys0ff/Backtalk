package off.kys.backtalk.presentation.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import off.kys.backtalk.R
import off.kys.backtalk.domain.model.MediaFolder
import off.kys.backtalk.domain.model.MediaItem
import off.kys.backtalk.domain.repository.MediaRepository
import off.kys.backtalk.presentation.event.MediaPickerEvent
import off.kys.backtalk.presentation.state.media.MediaPickerState

class MediaPickerViewModel(
    private val repository: MediaRepository,
    private val application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MediaPickerState())
    val uiState: StateFlow<MediaPickerState> = _uiState.asStateFlow()

    fun onEvent(event: MediaPickerEvent) {
        when (event) {
            is MediaPickerEvent.ToggleMediaSelection -> toggleSelection(event.uri, event.type)
            is MediaPickerEvent.SelectFolder -> selectFolder(event.folderId)
            is MediaPickerEvent.UpdateCaption -> _uiState.update { it.copy(captionText = event.text) }
            is MediaPickerEvent.PermissionsResult -> handlePermissionsResult(event.cameraGranted, event.mediaGranted)
            MediaPickerEvent.ClearSelection -> _uiState.update { it.copy(selectedUris = emptySet(), captionText = "") }
            MediaPickerEvent.RequestPermissions -> { /* Handled in UI via Launcher */ }
        }
    }

    private fun handlePermissionsResult(cameraGranted: Boolean, mediaGranted: Boolean) {
        _uiState.update { it.copy(hasCameraPermission = cameraGranted, hasMediaPermission = mediaGranted) }
        if (mediaGranted) {
            loadMedia()
        }
    }

    private fun loadMedia() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val media = repository.fetchGalleryMedia()
            val folders = generateFolders(media)
            _uiState.update {
                it.copy(
                    mediaItems = media,
                    folders = folders,
                    isLoading = false
                )
            }
            updateFilteredMedia()
        }
    }

    private fun generateFolders(media: List<MediaItem>): List<MediaFolder> {
        val folderList = mutableListOf<MediaFolder>()
        folderList.add(
            MediaFolder(
                id = "all",
                name = application.getString(R.string.media_folder_all),
                firstItemUri = media.firstOrNull()?.uri
            )
        )

        val bucketMap = mutableMapOf<String, MediaItem>()
        media.forEach { item ->
            if (item.bucketId != null && !bucketMap.containsKey(item.bucketId)) {
                bucketMap[item.bucketId] = item
            }
        }

        bucketMap.forEach { (id, item) ->
            folderList.add(
                MediaFolder(
                    id = id,
                    name = item.bucketName ?: "Unknown",
                    firstItemUri = item.uri
                )
            )
        }
        return folderList
    }

    private fun selectFolder(folderId: String) {
        _uiState.update { it.copy(selectedFolderId = folderId) }
        updateFilteredMedia()
    }

    private fun updateFilteredMedia() {
        _uiState.update { state ->
            val filtered = if (state.selectedFolderId == "all") {
                state.mediaItems
            } else {
                state.mediaItems.filter { it.bucketId == state.selectedFolderId }
            }
            state.copy(filteredMediaItems = filtered)
        }
    }

    private fun toggleSelection(uri: Uri, type: String) {
        _uiState.update { state ->
            val isAdding = uri !in state.selectedUris
            val newSelection = if (!isAdding) {
                state.selectedUris.filter { it != uri }.toSet()
            } else {
                state.selectedUris + uri
            }
            state.copy(
                selectedUris = newSelection,
                selectedType = type
            )
        }
    }
}