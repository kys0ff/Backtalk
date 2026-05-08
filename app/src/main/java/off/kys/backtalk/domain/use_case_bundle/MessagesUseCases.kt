package off.kys.backtalk.domain.use_case_bundle

import off.kys.backtalk.domain.use_case.CopyMessagesByIds
import off.kys.backtalk.domain.use_case.DeleteMessageById
import off.kys.backtalk.domain.use_case.GetAllMessages
import off.kys.backtalk.domain.use_case.GetMessageById
import off.kys.backtalk.domain.use_case.InsertMessage
import off.kys.backtalk.domain.use_case.ScheduleMessageUseCase

/**
 * A bundle of use cases related to message operations.
 *
 * This class serves as a convenient way to inject all message-related use cases
 * into ViewModels or other components.
 *
 * @property getAllMessages Use case to retrieve all messages.
 * @property getMessageById Use case to retrieve a specific message by its ID.
 * @property insertMessage Use case to insert a new message.
 * @property deleteMessageById Use case to delete a message by its ID.
 * @property copyMessagesByIds Use case to copy multiple messages by their IDs.
 * @property scheduleMessage Use case to schedule a message.
 */
data class MessagesUseCases(
    val getAllMessages: GetAllMessages,
    val getMessageById: GetMessageById,
    val insertMessage: InsertMessage,
    val deleteMessageById: DeleteMessageById,
    val copyMessagesByIds: CopyMessagesByIds,
    val scheduleMessage: ScheduleMessageUseCase
)
