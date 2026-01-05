package off.kys.backtalk.domain.use_case_bundle

import off.kys.backtalk.domain.use_case.DeleteMessageById
import off.kys.backtalk.domain.use_case.GetAllMessages
import off.kys.backtalk.domain.use_case.GetMessageById
import off.kys.backtalk.domain.use_case.InsertMessage

data class MessagesUseCases(
    val getAllMessages: GetAllMessages,
    val getMessageById: GetMessageById,
    val insertMessage: InsertMessage,
    val deleteMessageById: DeleteMessageById
)
