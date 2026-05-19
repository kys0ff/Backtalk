package off.kys.backtalk.presentation.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import off.kys.backtalk.common.manager.AlarmScheduler
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.domain.use_case_bundle.MessagesUseCases
import off.kys.backtalk.presentation.event.MessagesUiEvent
import off.kys.backtalk.presentation.state.MessagesUiState
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
    private val application: Application
) : ViewModel(), KoinComponent {

    private val alarmScheduler: AlarmScheduler by inject()

    private val _uiState = mutableStateOf(MessagesUiState())

    /**
     * The current UI state of the Messages screen, represented as a [State] of [MessagesUiState].
     */
    val uiState: State<MessagesUiState> = _uiState

    init {
        onEvent(MessagesUiEvent.LoadMessages)
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
            is MessagesUiEvent.SendVoiceMessage -> sendVoiceMessage(event.path, event.duration, event.waveform)
            is MessagesUiEvent.ReplyTo -> updateReply(event.message)
            is MessagesUiEvent.EditMessage -> updateEditingMessage(event.message)

            is MessagesUiEvent.ToggleSelection -> toggleSelection(event.id)
            is MessagesUiEvent.ClearSelection -> clearSelection()

            is MessagesUiEvent.DeleteSelected -> {
                _uiState.value = _uiState.value.copy(showDeleteConfirmation = true)
            }
            is MessagesUiEvent.ConfirmDeleteSelected -> deleteSelected()
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
                _uiState.value = _uiState.value.copy(showPinnedMessagesDialog = false)
            }
            is MessagesUiEvent.BlinkMessage -> {
                blinkMessage(event.id)
            }
            is MessagesUiEvent.ToggleMediaPicker -> {
                _uiState.value = _uiState.value.copy(showMediaPicker = event.show)
            }
            is MessagesUiEvent.SendMediaMessages -> {
                sendMediaMessages(event.uris, event.type)
            }
        }
    }

    private fun sendMediaMessages(uris: List<String>, type: String) {
        val replyTo = _uiState.value.replyingTo
        viewModelScope.launch {
            runCatching {
                val mediaPaths = uris.mapNotNull { uri ->
                    val sourceUri = Uri.parse(uri)
                    val extension = if (type.contains("video")) "mp4" else "jpg"
                    val fileName = "media_${System.currentTimeMillis()}_${sourceUri.lastPathSegment}.$extension"
                    val mediaDir = File(application.filesDir, "media").apply { mkdirs() }
                    val destFile = File(mediaDir, fileName)

                    application.contentResolver.openInputStream(sourceUri)?.use { input ->
                        destFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    destFile.absolutePath
                }

                if (mediaPaths.isNotEmpty()) {
                    // Split into multiple messages if more than 4 images
                    mediaPaths.chunked(4).forEachIndexed { index, chunk ->
                        useCases.insertMessage(
                            MessageEntity(
                                id = MessageId.generate(),
                                text = "", // No redundant [Image] text
                                timestamp = System.currentTimeMillis() + index,
                                repliedToId = replyTo?.id,
                                mediaPaths = chunk,
                                mediaType = type
                            )
                        )
                    }
                }
            }
        }
        updateReply(null)
        _uiState.value = _uiState.value.copy(showMediaPicker = false)
    }

    private fun blinkMessage(id: MessageId?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(blinkMessageId = id)
            if (id != null) {
                kotlinx.coroutines.delay(1920) // Match the delay in previous implementation
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
        
        val nextIndex = (state.activePinnedMessageIndex + 1) % state.pinnedMessages.size
        _uiState.value = state.copy(activePinnedMessageIndex = nextIndex)
    }

    /**
     * Loads all messages from the repository and updates the UI state.
     */
    private fun scheduleMessage(text: String, scheduledTime: Long) {
        if (!alarmScheduler.canScheduleExactAlarms()) {
            _uiState.value = _uiState.value.copy(showPermissionRationale = true)
            return
        }
        
        val replyTo = _uiState.value.replyingTo
        viewModelScope.launch {
            useCases.scheduleMessage(
                text = text,
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
                val pinnedMessages = sortedMessages.filter { it.isPinned }.reversed() // Newest pinned first for the bar
                
                _uiState.value = _uiState.value.copy(
                    messages = sortedMessages,
                    pinnedMessages = pinnedMessages,
                    isLoading = false,
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

        if (editingMessage != null) {
            viewModelScope.launch {
                useCases.insertMessage(
                    editingMessage.copy(
                        editedText = text,
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
                    text = text,
                    timestamp = System.currentTimeMillis(),
                    repliedToId = replyTo?.id
                )
            )
        }
        updateReply(null)
    }

    private fun sendVoiceMessage(path: String, duration: Long, waveform: List<Float>) {
        val replyTo = _uiState.value.replyingTo
        viewModelScope.launch {
            useCases.insertMessage(
                MessageEntity(
                    id = MessageId.generate(),
                    text = "[Voice Message]",
                    timestamp = System.currentTimeMillis(),
                    repliedToId = replyTo?.id,
                    voicePath = path,
                    voiceDuration = duration,
                    waveformData = waveform
                )
            )
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
     * Clears the selection of all messages.
     */
    private fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedMessageIds = emptySet())
    }

    /**
     * Deletes the selected messages.
     */
    private fun deleteSelected() {
        val ids = _uiState.value.selectedMessageIds
        viewModelScope.launch {
            ids.forEach { useCases.deleteMessageById(it) }
        }
        _uiState.value = _uiState.value.copy(showDeleteConfirmation = false)
        clearSelection()
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
            searchQuery = if (active) _uiState.value.searchQuery else "",
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

        _uiState.value = state.copy(currentSearchResultIndex = newIndex)
    }
}