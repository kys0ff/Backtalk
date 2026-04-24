package off.kys.backtalk.domain.use_case

import android.content.Context
import kotlinx.coroutines.flow.firstOrNull
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.domain.repository.MessagesRepository
import off.kys.backtalk.util.copyToClipboard

/**
 * Use case for copying the text content of multiple messages to the system clipboard.
 *
 * @property context The [Context] used to access the clipboard service.
 * @property repository The [MessagesRepository] to fetch messages from.
 */
class CopyMessagesByIds(
    private val context: Context,
    private val repository: MessagesRepository
) {
    /**
     * Fetches messages by their [ids], joins their text content, and copies it to the clipboard.
     *
     * The messages are sorted by their timestamp before being concatenated with a double newline separator.
     * If no messages are found for the provided [ids], the function returns without performing any action.
     *
     * @param ids A set of [MessageId]s to be copied.
     */
    suspend operator fun invoke(ids: Set<MessageId>) {
        val messages = repository.getMessagesByIds(ids).firstOrNull()?.toSet()?.sortedBy { it.timestamp }
            ?: return
        val messagesBody = messages.joinToString("\n\n") { it.text }
        // Copy the message to the clipboard
        context.copyToClipboard(messagesBody)
    }
}