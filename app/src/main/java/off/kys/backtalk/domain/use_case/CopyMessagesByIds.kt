package off.kys.backtalk.domain.use_case

import android.content.Context
import kotlinx.coroutines.flow.firstOrNull
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.domain.repository.MessagesRepository
import off.kys.backtalk.util.copyToClipboard

class CopyMessagesByIds(
    private val context: Context,
    private val repository: MessagesRepository
) {
    suspend operator fun invoke(ids: Set<MessageId>) {
        val messages = repository.getMessagesByIds(ids).firstOrNull()?.toSet() ?: return
        val messagesBody = messages.joinToString("\n\n") { it.text }
        // Copy the message to the clipboard
        context.copyToClipboard(messagesBody)
    }
}