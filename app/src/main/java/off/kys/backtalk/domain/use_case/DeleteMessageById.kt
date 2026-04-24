package off.kys.backtalk.domain.use_case

import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.domain.repository.MessagesRepository

/**
 * Use case for deleting a message by its identifier from the database.
 *
 * @property repository The [MessagesRepository] used to interact with message data.
 */
class DeleteMessageById(
    private val repository: MessagesRepository
) {
    /**
     * Executes the use case to delete a message with the given [id].
     *
     * @param id The identifier of the message to be deleted.
     */
    suspend operator fun invoke(id: MessageId) = repository.deleteMessageById(id)
}
