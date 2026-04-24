package off.kys.backtalk.domain.use_case

import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.domain.repository.MessagesRepository

/**
 * Use case to retrieve a single message by its unique identifier.
 *
 * @property repository The [MessagesRepository] used to fetch the message.
 */
class GetMessageById(
    private val repository: MessagesRepository
) {
    /**
     * Executes the use case to fetch a message.
     *
     * @param id The [MessageId] of the message to retrieve.
     * @return The message with the given [id], or null if no such message exists.
     */
    suspend operator fun invoke(id: MessageId) = repository.getMessageById(id)
}