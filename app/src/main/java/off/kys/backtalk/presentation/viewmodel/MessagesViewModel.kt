package off.kys.backtalk.presentation.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.domain.use_case_bundle.MessagesUseCases

class MessagesViewModel(
    private val useCases: MessagesUseCases
) : ViewModel() {

    private val _uiState = mutableStateOf(MessagesUiState())
    val uiState: State<MessagesUiState> = _uiState

    init {
        onEvent(MessagesUiEvent.LoadMessages)
    }

    fun onEvent(event: MessagesUiEvent) {
        when (event) {
            is MessagesUiEvent.LoadMessages -> loadMessages()
            is MessagesUiEvent.SendMessage -> sendMessage(event.text)
            is MessagesUiEvent.ReplyTo -> updateReply(event.message)
            is MessagesUiEvent.SelectMessage -> updateSelection(event.id)
            is MessagesUiEvent.DeleteMessage -> deleteMessage(event.id)
        }
    }

    private fun loadMessages() {
        viewModelScope.launch {
            useCases.getAllMessages().collect { messages ->
                _uiState.value = _uiState.value.copy(
                    messages = messages.sortedBy { it.timestamp }
                )
            }
        }
    }

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

    private fun updateReply(message: MessageEntity?) {
        _uiState.value = _uiState.value.copy(replyingTo = message)
    }

    private fun updateSelection(id: MessageId?) {
        _uiState.value = _uiState.value.copy(selectedMessageId = id)
    }

    private fun deleteMessage(id: MessageId) {
        viewModelScope.launch {
            useCases.deleteMessageById(id)
        }
        updateSelection(null)
    }
}

data class MessagesUiState(
    val messages: List<MessageEntity> = emptyList(),
    val replyingTo: MessageEntity? = null,
    val selectedMessageId: MessageId? = null
)

sealed interface MessagesUiEvent {
    data class SendMessage(val text: String) : MessagesUiEvent
    data class ReplyTo(val message: MessageEntity?) : MessagesUiEvent
    data class SelectMessage(val id: MessageId?) : MessagesUiEvent
    data class DeleteMessage(val id: MessageId) : MessagesUiEvent
    data object LoadMessages : MessagesUiEvent
}