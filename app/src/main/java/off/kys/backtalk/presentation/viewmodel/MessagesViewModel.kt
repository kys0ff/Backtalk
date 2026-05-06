package off.kys.backtalk.presentation.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.domain.use_case_bundle.MessagesUseCases
import off.kys.backtalk.presentation.event.MessagesUiEvent
import off.kys.backtalk.presentation.state.MessagesUiState

/**
 * ViewModel for the Messages screen.
 *
 * This ViewModel handles the UI logic and state management for displaying,
 * sending, editing, deleting, and selecting messages.
 *
 * @param useCases The bundle of use cases related to messages.
 */
class MessagesViewModel(
    private val useCases: MessagesUseCases
) : ViewModel() {

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
            is MessagesUiEvent.ReplyTo -> updateReply(event.message)
            is MessagesUiEvent.EditMessage -> updateEditingMessage(event.message)

            is MessagesUiEvent.ToggleSelection -> toggleSelection(event.id)
            is MessagesUiEvent.ClearSelection -> clearSelection()

            is MessagesUiEvent.DeleteSelected -> deleteSelected()
            is MessagesUiEvent.CopySelected -> copySelected()

            is MessagesUiEvent.ToggleSearch -> toggleSearch(event.active)
            is MessagesUiEvent.UpdateSearchQuery -> updateSearchQuery(event.query)
            is MessagesUiEvent.NavigateSearch -> navigateSearch(event.up)
        }
    }

    /**
     * Loads all messages from the repository and updates the UI state.
     */
    private fun loadMessages() {
        viewModelScope.launch {
            useCases.getAllMessages().collect { messages ->
                _uiState.value = _uiState.value.copy(
                    messages = messages.sortedBy { it.timestamp }
                )
            }
        }
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