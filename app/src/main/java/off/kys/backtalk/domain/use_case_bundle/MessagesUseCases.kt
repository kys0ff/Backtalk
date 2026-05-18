package off.kys.backtalk.domain.use_case_bundle

import off.kys.backtalk.domain.use_case.CancelScheduledMessage
import off.kys.backtalk.domain.use_case.CopyMessagesByIds
import off.kys.backtalk.domain.use_case.DeleteMessageById
import off.kys.backtalk.domain.use_case.GetAllMessages
import off.kys.backtalk.domain.use_case.GetAllScheduledMessages
import off.kys.backtalk.domain.use_case.GetMessageById
import off.kys.backtalk.domain.use_case.InsertMessage
import off.kys.backtalk.domain.use_case.ScheduleMessageUseCase

/**
 * A bundle of use cases related to message operations.
 *
 * This class serves as a convenient way to inject all message-related use cases
 * into ViewModels or other components.
 */
data class MessagesUseCases(
    val getAllMessages: GetAllMessages,
    val getMessageById: GetMessageById,
    val insertMessage: InsertMessage,
    val deleteMessageById: DeleteMessageById,
    val copyMessagesByIds: CopyMessagesByIds,
    val scheduleMessage: ScheduleMessageUseCase,
    val getAllScheduledMessages: GetAllScheduledMessages,
    val cancelScheduledMessage: CancelScheduledMessage
)
