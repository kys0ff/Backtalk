package off.kys.backtalk.domain.use_case_bundle

import off.kys.backtalk.domain.use_case.CopyMessagesByIds
import off.kys.backtalk.domain.use_case.DeleteMessageById
import off.kys.backtalk.domain.use_case.GetAllMessages
import off.kys.backtalk.domain.use_case.GetMessageById
import off.kys.backtalk.domain.use_case.InsertMessage

/**
 * Data class representing a bundle of use cases related to messages.
 *
 * @property getAllMessages The use case to get all messages.
 * @property getMessageById The use case to get a message by its ID.
 * @property insertMessage The use case to insert a new message.
 * @property deleteMessageById The use case to delete a message by its ID.
 * @property copyMessagesByIds The use case to copy messages by their IDs.
 */
data class MessagesUseCases(
    val getAllMessages: GetAllMessages,
    val getMessageById: GetMessageById,
    val insertMessage: InsertMessage,
    val deleteMessageById: DeleteMessageById,
    val copyMessagesByIds: CopyMessagesByIds
)
