package off.kys.backtalk.presentation.state

import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId

/**
 * Data class representing the state of the messages screen.
 *
 * @param messages The list of messages to display.
 * @param replyingTo The message being replied to, if any.
 * @param selectedMessageId The ID of the currently selected message, if any.
 */
data class MessagesUiState(
    val messages: List<MessageEntity> = emptyList(),
    val replyingTo: MessageEntity? = null,
    val selectedMessageId: MessageId? = null
)