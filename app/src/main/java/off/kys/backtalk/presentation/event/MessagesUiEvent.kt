package off.kys.backtalk.presentation.event

import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId

/**
 * Sealed interface representing the different UI events related to messages.
 */
sealed interface MessagesUiEvent {
    /**
     * UI event to send a message.
     *
     * @param text The text of the message to send.
     */
    data class SendMessage(val text: String) : MessagesUiEvent

    /**
     * UI event to reply to a message.
     *
     * @param message The message to reply to, or null if no message is being replied to.
     */
    data class ReplyTo(val message: MessageEntity?) : MessagesUiEvent

    /**
     * UI event to select a message by its ID.
     *
     * @param id The ID of the message to select.
     */
    data class SelectMessage(val id: MessageId?) : MessagesUiEvent

    /**
     * UI event to delete a message by its ID.
     *
     * @param id The ID of the message to delete.
     */
    data class DeleteMessage(val id: MessageId) : MessagesUiEvent

    /**
     * UI event to copy a message by its ID.
     *
     * @param id The ID of the message to copy.
     */
    data class CopyMessage(val id: MessageId) : MessagesUiEvent

    /**
     * UI event to load messages from the data source.
     */
    data object LoadMessages : MessagesUiEvent
}