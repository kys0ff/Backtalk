package off.kys.backtalk.domain.use_case

import android.content.Context
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.domain.repository.MessagesRepository
import off.kys.backtalk.util.copyToClipboard

class CopyMessageById(
    private val context: Context,
    private val repository: MessagesRepository
) {
    suspend operator fun invoke(id: MessageId) {
        val message = repository.getMessageById(id)
        // Copy the message to the clipboard
        message?.let { messageEntity ->
            context.copyToClipboard(messageEntity.text)
        }
    }
}