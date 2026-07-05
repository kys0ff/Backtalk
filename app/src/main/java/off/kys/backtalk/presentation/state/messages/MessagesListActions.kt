package off.kys.backtalk.presentation.state.messages

import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.presentation.model.MessageUiModel

interface MessagesListActions {
    fun onReply(message: MessageUiModel)
    fun onEdit(message: MessageUiModel)
    fun onDelete(message: MessageUiModel)
    fun onCopy(message: MessageUiModel)
    fun onTogglePin(message: MessageUiModel)
    fun onToggleSelect(messageId: MessageId)
    fun onLongClick(message: MessageUiModel?)
    fun onScrollToMessage(messageId: MessageId)
    fun onTagClick(tag: String)
    fun onToggleImageSelect(messageId: MessageId, path: String)
    fun onMarkSwipeHintShown()
}