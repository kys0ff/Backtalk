package off.kys.backtalk.domain.use_case

import android.content.Context
import kotlinx.coroutines.flow.firstOrNull
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.domain.repository.MessagesRepository
import off.kys.backtalk.util.copyToClipboard

/**
 * Use case to copy the text content of multiple messages to the system clipboard.
 *
 * @property context The [Context] used to access clipboard services.
 * @property repository The [MessagesRepository] used to retrieve message data.
 */
class CopyMessagesByIds(
    private val context: Context,
    private val repository: MessagesRepository
) {
    /**
     * Fetches messages by their [ids], sorts them by timestamp, and copies their text to the clipboard.
     *
     * @param ids A set of [MessageId]s identifying the messages to be copied.
     */
    suspend operator fun invoke(ids: Set<MessageId>) {
        val messages = repository.getMessagesByIds(ids).firstOrNull()?.toSet()?.sortedBy { it.timestamp }
            ?: return
        val messagesBody = messages.joinToString("\n\n") { it.text }
        // Copy the message to the clipboard
        context.copyToClipboard(messagesBody)
    }
}