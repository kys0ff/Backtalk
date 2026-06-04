package off.kys.backtalk.presentation.viewmodel

import android.app.Application
import android.webkit.MimeTypeMap
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import off.kys.backtalk.BuildConfig
import off.kys.backtalk.R
import off.kys.backtalk.common.Constants
import off.kys.backtalk.common.manager.AlarmScheduler
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.domain.use_case_bundle.MessagesUseCases
import off.kys.backtalk.presentation.event.MessagesUiEvent
import off.kys.backtalk.presentation.state.MessagesUiState
import off.kys.backtalk.util.HashUtils
import off.kys.backtalk.util.MediaUtils
import off.kys.backtalk.util.emptyString
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

/**
 * ViewModel for the Messages screen.
 *
 * This ViewModel handles the UI logic and state management for displaying,
 * sending, editing, deleting, and selecting messages.
 *
 * @param useCases The bundle of use cases related to messages.
 */
class MessagesViewModel(
    private val useCases: MessagesUseCases,
    private val preferences: BacktalkPreferences,
    private val application: Application
) : AndroidViewModel(application), KoinComponent {

    private val alarmScheduler: AlarmScheduler by inject()

    private val _uiState = mutableStateOf(MessagesUiState())

    /**
     * The current UI state of the Messages screen, represented as a [State] of [MessagesUiState].
     */
    val uiState: State<MessagesUiState> = _uiState

    private var blinkJob: Job? = null

    init {
        onEvent(MessagesUiEvent.LoadMessages)
        checkChangelog()
    }

    private fun checkChangelog() {
        if (preferences.lastSeenChangelogVersion != BuildConfig.VERSION_NAME) {
            _uiState.value = _uiState.value.copy(showChangelogDialog = true)
        }
    }

    /**
     * Handles UI events related to messages.
     *
     * @param event The UI event to handle.
     * @see MessagesUiEvent
     */
    fun onEvent(event: MessagesUiEvent) {
        when (event) {
            is MessagesUiEvent.LoadMessages -> loadMessages()
            is MessagesUiEvent.SendMessage -> sendMessage(event.text)
            is MessagesUiEvent.SendVoiceMessage -> sendVoiceMessage(
                event.path,
                event.duration,
                event.waveform
            )

            is MessagesUiEvent.ReplyTo -> updateReply(event.message)
            is MessagesUiEvent.EditMessage -> updateEditingMessage(event.message)

            is MessagesUiEvent.ToggleSelection -> toggleSelection(event.id)
            is MessagesUiEvent.ClearSelection -> clearSelection()

            is MessagesUiEvent.DeleteSelected -> {
                _uiState.value = _uiState.value.copy(showDeleteConfirmation = true)
            }

            is MessagesUiEvent.ConfirmDeleteSelected -> {
                deleteSelected()
                deleteSelectedImages()
                _uiState.value = _uiState.value.copy(showDeleteConfirmation = false)
            }
            is MessagesUiEvent.DismissDeleteConfirmation -> {
                _uiState.value = _uiState.value.copy(showDeleteConfirmation = false)
            }

            is MessagesUiEvent.CopySelected -> copySelected()

            is MessagesUiEvent.ToggleSearch -> toggleSearch(event.active)
            is MessagesUiEvent.UpdateSearchQuery -> updateSearchQuery(event.query)
            is MessagesUiEvent.NavigateSearch -> navigateSearch(event.up)
            is MessagesUiEvent.ScheduleMessage -> scheduleMessage(event.text, event.scheduledTime)
            MessagesUiEvent.DismissPermissionRationale -> {
                _uiState.value = _uiState.value.copy(showPermissionRationale = false)
            }

            is MessagesUiEvent.SelectTag -> {
                val newTag = if (_uiState.value.selectedTag == event.tag) null else event.tag
                _uiState.value = _uiState.value.copy(selectedTag = newTag)
                updateFilteredMessages()
            }

            is MessagesUiEvent.TogglePinMessage -> togglePinMessage(event.id, event.isPinned)
            is MessagesUiEvent.NavigatePinned -> navigatePinned()
            is MessagesUiEvent.TogglePinnedMessagesDialog -> {
                _uiState.value = _uiState.value.copy(showPinnedMessagesDialog = event.show)
            }

            is MessagesUiEvent.ScrollToMessage -> {
                val state = _uiState.value
                val pinnedIndex = state.pinnedMessages.indexOfFirst { it.id == event.id }
                _uiState.value = state.copy(
                    showPinnedMessagesDialog = false,
                    selectedTag = null,
                    activePinnedMessageIndex = if (pinnedIndex != -1) pinnedIndex else state.activePinnedMessageIndex
                )
                updateFilteredMessages()
            }

            is MessagesUiEvent.BlinkMessage -> {
                blinkMessage(event.id)
            }

            is MessagesUiEvent.ToggleMediaPicker -> {
                _uiState.value = _uiState.value.copy(showMediaPicker = event.show)
            }

            is MessagesUiEvent.SendMediaMessages -> {
                sendMediaMessages(event.uris, event.type, event.description)
            }

            MessagesUiEvent.ConsumedScrollToBottom -> {
                _uiState.value = _uiState.value.copy(shouldScrollToBottom = false)
            }

            MessagesUiEvent.ConsumedScrollToPinned -> {
                _uiState.value = _uiState.value.copy(shouldScrollToPinned = false)
            }

            is MessagesUiEvent.RemoveImageFromMessage -> removeImageFromMessage(event.messageId, event.imagePath)
            is MessagesUiEvent.ToggleImageSelection -> toggleImageSelection(event.messageId, event.imagePath)
            is MessagesUiEvent.DeleteSelectedImages -> {
                _uiState.value = _uiState.value.copy(showDeleteConfirmation = true)
            }
            is MessagesUiEvent.ClearImageSelection -> clearImageSelection()
            MessagesUiEvent.DismissChangelog -> {
                preferences.lastSeenChangelogVersion = BuildConfig.VERSION_NAME
                _uiState.value = _uiState.value.copy(showChangelogDialog = false)
            }
        }
    }

    private fun sendMediaMessages(uris: List<String>, type: String, description: String?) {
        val replyTo = _uiState.value.replyingTo
        val removeMetadata = preferences.removeImageMetadataEnabled && type.startsWith("image/")
        val smartPointing = preferences.smartImagePointingEnabled

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val mediaPaths = uris.mapNotNull { uri ->
                    val sourceUri = uri.toUri()
                    val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(type)
                        ?: if (type.contains("video")) "mp4" else "jpg"

                    val mediaDir = File(application.filesDir, "media").apply { mkdirs() }

                    val fileName = if (smartPointing) {
                        val hash = application.contentResolver.openInputStream(sourceUri)?.use {
                            HashUtils.calculateSha256(it)
                        } ?: System.currentTimeMillis().toString()
                        "media_$hash.$extension"
                    } else {
                        "media_${System.currentTimeMillis()}_${sourceUri.lastPathSegment}.$extension"
                    }

                    val destFile = File(mediaDir, fileName)

                    // If smart pointing and file exists, reuse it
                    if (smartPointing && destFile.exists()) {
                        return@mapNotNull destFile.absolutePath
                    }

                    application.contentResolver.openInputStream(sourceUri)?.use { input ->
                        destFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    if (removeMetadata) {
                        MediaUtils.stripImageMetadata(destFile)
                    }

                    destFile.absolutePath
                }

                if (mediaPaths.isNotEmpty()) {
                    val chunks = mediaPaths.chunked(4)
                    chunks.forEachIndexed { index, chunk ->
                        val isLastChunk = index == chunks.size - 1
                        val defaultCaption = when {
                            type.startsWith("image/") -> application.getString(R.string.chat_media_image)
                            type.startsWith("video/") -> application.getString(R.string.chat_media_video)
                            else -> application.getString(R.string.chat_media_general)
                        }

                        useCases.insertMessage(
                            MessageEntity(
                                id = MessageId.generate(),
                                text = if (isLastChunk) {
                                    if (description.isNullOrBlank()) defaultCaption else description
                                } else emptyString(),
                                timestamp = System.currentTimeMillis() + index,
                                repliedToId = replyTo?.id,
                                mediaPaths = chunk,
                                mediaType = type
                            )
                        )
                    }
                }
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(shouldScrollToBottom = true)
                }
            }
        }
        updateReply(null)
        _uiState.value = _uiState.value.copy(showMediaPicker = false)
    }

    private fun blinkMessage(id: MessageId?) {
        blinkJob?.cancel()
        blinkJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(blinkMessageId = id)
            if (id != null) {
                delay(1920)
                _uiState.value = _uiState.value.copy(blinkMessageId = null)
            }
        }
    }

    private fun togglePinMessage(id: MessageId, isPinned: Boolean) {
        viewModelScope.launch {
            useCases.togglePinMessage(id, isPinned)
        }
    }

    private fun navigatePinned() {
        val state = _uiState.value
        if (state.pinnedMessages.isEmpty()) return

        val nextIndex = if (state.pinnedMessages.size > 1) {
            (state.activePinnedMessageIndex + 1) % state.pinnedMessages.size
        } else {
            0
        }

        _uiState.value = state.copy(
            activePinnedMessageIndex = nextIndex,
            shouldScrollToPinned = true,
            selectedTag = null
        )
        updateFilteredMessages()
    }

    /**
     * Loads all messages from the repository and updates the UI state.
     */
    private fun scheduleMessage(text: String, scheduledTime: Long) {
        if (!alarmScheduler.canScheduleExactAlarms()) {
            _uiState.value = _uiState.value.copy(showPermissionRationale = true)
            return
        }

        val trimmedText = if (preferences.trimMessagesEnabled) text.trim() else text
        val replyTo = _uiState.value.replyingTo
        viewModelScope.launch {
            useCases.scheduleMessage(
                text = trimmedText,
                scheduledTime = scheduledTime,
                repliedToId = replyTo?.id
            )
        }
        updateReply(null)
    }

    private fun loadMessages() {
        viewModelScope.launch {
            useCases.getAllMessages().collect { messages ->
                val sortedMessages = messages.sortedBy { it.timestamp }
                val pinnedMessages = sortedMessages.filter { it.isPinned }.reversed()

                _uiState.value = _uiState.value.copy(
                    messages = sortedMessages,
                    pinnedMessages = pinnedMessages,
                    isLoading = false,
                    shouldScrollToPinned = false,
                    activePinnedMessageIndex = if (pinnedMessages.isEmpty()) 0 else _uiState.value.activePinnedMessageIndex % pinnedMessages.size
                )
                updateFilteredMessages()
            }
        }
    }

    private fun updateFilteredMessages() {
        val state = _uiState.value
        val filtered = if (state.selectedTag == null) {
            state.messages
        } else {
            state.messages.filter { message ->
                val text = message.editedText ?: message.text
                text.contains("#${state.selectedTag}", ignoreCase = true)
            }.ifEmpty { state.messages }
        }
        _uiState.value = _uiState.value.copy(filteredMessages = filtered)
    }

    /**
     * Sends a message with the given text or updates an existing message if editing.
     *
     * @param text The text of the message to send or the new text for the edited message.
     */
    private fun sendMessage(text: String) {
        val editingMessage = _uiState.value.editingMessage
        val trimmedText = if (preferences.trimMessagesEnabled) text.trim() else text

        if (editingMessage != null) {
            val currentTime = System.currentTimeMillis()
            if ((currentTime - editingMessage.timestamp) >= Constants.MESSAGE_EDIT_DELETE_WINDOW) {
                updateEditingMessage(null)
                return
            }
            viewModelScope.launch {
                val previousVisibleText = editingMessage.editedText ?: editingMessage.text
                useCases.insertMessage(
                    editingMessage.copy(
                        editedText = trimmedText,
                        text = previousVisibleText,
                        editedAt = System.currentTimeMillis()
                    )
                )
            }
            updateEditingMessage(null)
            return
        }

        val replyTo = _uiState.value.replyingTo
        viewModelScope.launch {
            useCases.insertMessage(
                MessageEntity(
                    id = MessageId.generate(),
                    text = trimmedText,
                    timestamp = System.currentTimeMillis(),
                    repliedToId = replyTo?.id
                )
            )
            _uiState.value = _uiState.value.copy(shouldScrollToBottom = true)
        }
        updateReply(null)
    }

    private fun removeImageFromMessage(messageId: MessageId, imagePath: String) {
        val message = _uiState.value.messages.find { it.id == messageId }
        val currentTime = System.currentTimeMillis()
        if (message != null && (currentTime - message.timestamp) < Constants.MESSAGE_EDIT_DELETE_WINDOW) {
            viewModelScope.launch {
                useCases.removeImageFromMessage(messageId, imagePath)
            }
        }
    }

    private fun toggleImageSelection(messageId: MessageId, imagePath: String) {
        val currentMap = _uiState.value.selectedImagePaths
        val currentSet = currentMap[messageId] ?: emptySet()
        val newSet = if (imagePath in currentSet) currentSet - imagePath else currentSet + imagePath
        
        _uiState.value = _uiState.value.copy(
            selectedImagePaths = if (newSet.isEmpty()) currentMap - messageId else currentMap + (messageId to newSet)
        )
    }

    private fun deleteSelected() {
        val ids = _uiState.value.selectedMessageIds
        val messages = _uiState.value.messages
        val currentTime = System.currentTimeMillis()

        viewModelScope.launch {
            ids.forEach { id ->
                val message = messages.find { it.id == id }
                if (message != null) {
                    val isWithinWindow = (currentTime - message.timestamp) < Constants.MESSAGE_EDIT_DELETE_WINDOW
                    if (isWithinWindow) {
                        useCases.deleteMessageById(id)
                    }
                }
            }
        }
        _uiState.value = _uiState.value.copy(selectedMessageIds = emptySet())
    }

    private fun deleteSelectedImages() {
        val selectedImagePaths = _uiState.value.selectedImagePaths
        val selectedMessageIds = _uiState.value.selectedMessageIds
        val messages = _uiState.value.messages
        val currentTime = System.currentTimeMillis()

        viewModelScope.launch {
            selectedImagePaths.forEach { (messageId, paths) ->
                val message = messages.find { it.id == messageId }
                if (messageId !in selectedMessageIds && message != null) {
                    val isWithinWindow = (currentTime - message.timestamp) < Constants.MESSAGE_EDIT_DELETE_WINDOW
                    if (isWithinWindow) {
                        useCases.removeImagesFromMessage(messageId, paths)
                    }
                }
            }
        }
        _uiState.value = _uiState.value.copy(selectedImagePaths = emptyMap())
    }

    private fun clearImageSelection() {
        _uiState.value = _uiState.value.copy(
            selectedImagePaths = emptyMap()
        )
    }

    private fun sendVoiceMessage(path: String, duration: Long, waveform: List<Float>) {
        val replyTo = _uiState.value.replyingTo
        viewModelScope.launch {
            useCases.insertMessage(
                MessageEntity(
                    id = MessageId.generate(),
                    text = application.getString(R.string.chat_media_voice),
                    timestamp = System.currentTimeMillis(),
                    repliedToId = replyTo?.id,
                    voicePath = path,
                    voiceDuration = duration,
                    waveformData = waveform
                )
            )
            _uiState.value = _uiState.value.copy(shouldScrollToBottom = true)
        }
        updateReply(null)
    }

    /**
     * Updates the UI state with the given message as the message being edited.
     *
     * @param message The message to set as the editing message, or null to stop editing.
     */
    private fun updateEditingMessage(message: MessageEntity?) {
        _uiState.value = _uiState.value.copy(editingMessage = message, replyingTo = null)
    }

    /**
     * Updates the UI state with the given message as the message being replied to.
     *
     * @param message The message to set as the replying to, or null to stop replying.
     */
    private fun updateReply(message: MessageEntity?) {
        _uiState.value = _uiState.value.copy(replyingTo = message, editingMessage = null)
    }

    /**
     * Toggles the selection of a message with the given ID.
     *
     * @param id The ID of the message to toggle selection for.
     */
    private fun toggleSelection(id: MessageId) {
        val current = _uiState.value.selectedMessageIds
        _uiState.value = _uiState.value.copy(
            selectedMessageIds =
                if (id in current) current - id else current + id
        )
    }

    /**
     * Clears the selection of all messages and images.
     */
    private fun clearSelection() {
        _uiState.value = _uiState.value.copy(
            selectedMessageIds = emptySet(),
            selectedImagePaths = emptyMap()
        )
    }

    /**
     * Copies the selected messages to the clipboard.
     */
    private fun copySelected() {
        val selectedIds = _uiState.value.selectedMessageIds
        viewModelScope.launch {
            useCases.copyMessagesByIds(selectedIds)
        }
        clearSelection()
    }

    /**
     * Toggles the search mode.
     *
     * @param active Whether the search mode should be active.
     */
    private fun toggleSearch(active: Boolean) {
        _uiState.value = _uiState.value.copy(
            isSearchActive = active,
            searchQuery = if (active) _uiState.value.searchQuery else emptyString(),
            searchResults = if (active) _uiState.value.searchResults else emptyList(),
            currentSearchResultIndex = if (active) _uiState.value.currentSearchResultIndex else -1
        )
    }

    /**
     * Updates the search query and performs a search.
     *
     * @param query The new search query.
     */
    private fun updateSearchQuery(query: String) {
        val previousQuery = _uiState.value.searchQuery

        if (query == previousQuery) return

        val results = if (query.isBlank()) {
            emptyList()
        } else {
            val terms = query.trim().lowercase().split(Regex("\\s+"))

            _uiState.value.messages.asSequence()
                .filter { message ->
                    val text = (message.editedText ?: message.text).lowercase()
                    terms.all { term -> text.contains(term) }
                }
                .map { it.id }
                .toList()
                .reversed()
        }

        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            searchResults = results,
            currentSearchResultIndex = if (results.isNotEmpty()) 0 else -1
        )
    }

    /**
     * Navigates through search results.
     *
     * @param up Whether to navigate to the previous result (up) or next result (down).
     */
    private fun navigateSearch(up: Boolean) {
        val state = _uiState.value
        if (state.searchResults.isEmpty()) return

        val newIndex = if (up) {
            (state.currentSearchResultIndex + 1) % state.searchResults.size
        } else {
            (state.currentSearchResultIndex - 1 + state.searchResults.size) % state.searchResults.size
        }

        _uiState.value = state.copy(
            currentSearchResultIndex = newIndex,
            scrollToSearchTrigger = state.scrollToSearchTrigger + 1
        )
    }
}
