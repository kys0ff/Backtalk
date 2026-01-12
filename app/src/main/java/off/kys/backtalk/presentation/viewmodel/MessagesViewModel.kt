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

class MessagesViewModel(
    private val useCases: MessagesUseCases
) : ViewModel() {

    private val _uiState = mutableStateOf(MessagesUiState())
    val uiState: State<MessagesUiState> = _uiState

    init {
        onEvent(MessagesUiEvent.LoadMessages)
    }

    /**
     * Handles UI events related to messages.
     *
     * @param event The UI event to handle.
     */
    fun onEvent(event: MessagesUiEvent) {
        when (event) {
            is MessagesUiEvent.LoadMessages -> loadMessages()
            is MessagesUiEvent.SendMessage -> sendMessage(event.text)
            is MessagesUiEvent.ReplyTo -> updateReply(event.message)

            is MessagesUiEvent.ToggleSelection -> toggleSelection(event.id)
            is MessagesUiEvent.ClearSelection -> clearSelection()

            is MessagesUiEvent.DeleteSelected -> deleteSelected()
            is MessagesUiEvent.CopySelected -> copySelected()
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
     * Sends a message with the given text.
     *
     * @param text The text of the message to send.
     */
    private fun sendMessage(text: String) {
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
     * Updates the UI state with the given message as the replying to message.
     *
     * @param message The message to set as the replying to
     */
    private fun updateReply(message: MessageEntity?) {
        _uiState.value = _uiState.value.copy(replyingTo = message)
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
}