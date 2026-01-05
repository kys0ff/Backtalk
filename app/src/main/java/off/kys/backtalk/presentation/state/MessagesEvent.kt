package off.kys.backtalk.presentation.state

import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId

/**
 * Events for the Messages screen.
 */
sealed class MessagesEvent {
    /**
     * Event to fetch all messages from the database.
     */
    object GetAllMessages: MessagesEvent()

    /**
     * Event to fetch a message by its ID from the database.
     * @param id The ID of the message to fetch.
     */
    data class GetMessageById(val id: MessageId): MessagesEvent()

    /**
     * Event to insert a new message into the database.
     * @param messageEntity The message to insert.
     */
    data class InsertMessage(val messageEntity: MessageEntity): MessagesEvent()

    /**
     * Event to delete a message by its ID from the database.
     * @param id The ID of the message to delete.
     */
    data class DeleteMessageById(val id: MessageId): MessagesEvent()
}